import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, Observable, of } from 'rxjs';

type Product = { id: string; sku: string; name: string; category: string; currentPrice: number; stockLevel: number; reorderThreshold: number; demandVelocity: number; status: string };
type Pricing = { id: string; productId: string; recommendedPrice: number; direction: string; confidence: number; reasoning: string; status: string; triggerReason: string };
type Reorder = { id: string; productId: string; recommendedQuantity: number; confidence: number; reasoning: string; status: string; triggerReason: string };
type Strategy = { mode: string; aiConfigured: boolean };

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule], template: `
<main><div class="notice loading" *ngIf="loading">Refreshing products and review queue...</div><div class="notice error" *ngIf="error">{{ error }} <button (click)="load()">Try again</button></div><header><div class="brand"><span>SP</span><div><strong>StockPulse</strong><small>ShopStream merchandising desk</small></div></div><div class="live"><i></i><label for="strategy-mode">Advisor</label><select id="strategy-mode" [ngModel]="strategyMode" (ngModelChange)="changeStrategy($event)" [disabled]="busy || !strategyAvailable"><option value="RULE">Rules</option><option value="AI">AI</option></select><small *ngIf="!strategyAvailable">Restart backend for mode control</small><small *ngIf="strategyMode === 'AI' && !aiConfigured">Rule fallback: API key missing</small></div></header>
<section class="intro"><p class="eyebrow">DECISION SUPPORT / {{ today | date:'MMM d, y' }}</p><h1>Make the next move<br><em>before stock does.</em></h1><p class="lede">A clear view of inventory signals and human-approved recommendations.</p></section>
<section class="toolbar"><div><h2>Product watchlist</h2><p>{{ products.length }} products monitored</p></div><button class="primary" (click)="showCreate = !showCreate">+ Add product</button></section>
<form class="create" *ngIf="showCreate" (ngSubmit)="createProduct()"><input [(ngModel)]="draft.name" name="name" placeholder="Product name" required><input [(ngModel)]="draft.sku" name="sku" placeholder="SKU" required><select [(ngModel)]="draft.category" name="category"><option value="UNSPECIFIED">Category</option><option value="ELECTRONICS">Electronics</option><option value="APPAREL">Apparel</option><option value="HOME">Home</option></select><input [(ngModel)]="draft.currentPrice" name="price" type="number" min="0.01" step="0.01" placeholder="Price" required><input [(ngModel)]="draft.stockLevel" name="stock" type="number" min="0" step="1" placeholder="Stock" required><input [(ngModel)]="draft.reorderThreshold" name="threshold" type="number" min="0" step="1" placeholder="Reorder threshold" required><button class="primary" [disabled]="busy">Create</button></form>
<section class="grid"><article class="product" *ngFor="let product of products"><div class="product-top"><div><span class="sku">{{ product.sku }}</span><h3>{{ product.name }}</h3><span class="category">{{ product.category }}</span></div><span class="status" [class.alert]="product.status !== 'ACTIVE'">{{ product.status.replace('_', ' ') }}</span></div><div class="metrics"><div><small>PRICE</small><strong>$ {{ product.currentPrice | number:'1.2-2' }}</strong></div><div><small>ON HAND</small><strong>{{ product.stockLevel }} <small>/ {{ product.reorderThreshold }} min</small></strong></div><div><small>VELOCITY</small><strong>{{ product.demandVelocity }}<small> units</small></strong></div></div><div class="actions"><button (click)="order(product)" [disabled]="busy || product.stockLevel === 0">Simulate sale</button><button (click)="manual(product)" [disabled]="busy">Suggest price</button><button (click)="manualReorder(product)" [disabled]="busy">Suggest reorder</button><button class="remove" (click)="remove(product)" [disabled]="busy">Remove</button></div></article></section>
<section class="review"><div class="toolbar"><div><h2>Review queue</h2><p>Nothing changes without your approval.</p></div><button class="refresh" (click)="load()">Refresh queue</button></div><div class="suggestions" *ngIf="pricing.length || reorder.length; else empty"><div class="suggestion" *ngFor="let item of pricing"><div class="suggestion-head"><span class="type pricing-type">PRICING</span><span class="trigger">{{ item.triggerReason }}</span></div><h3>{{ productName(item.productId) }} <b>{{ item.direction }}</b> to $ {{ item.recommendedPrice | number:'1.2-2' }}</h3><p>{{ item.reasoning }}</p><div class="suggestion-foot"><span>{{ item.confidence * 100 | number:'1.0-0' }}% confidence</span><button class="reject" (click)="decide('pricing-suggestions', item.id, 'REJECTED')">Reject</button><button class="approve" (click)="decide('pricing-suggestions', item.id, 'ACCEPTED')">Approve price</button></div></div><div class="suggestion" *ngFor="let item of reorder"><div class="suggestion-head"><span class="type reorder-type">REORDER</span><span class="trigger">{{ item.triggerReason }}</span></div><h3>{{ productName(item.productId) }} <b>+{{ item.recommendedQuantity }} units</b></h3><p>{{ item.reasoning }}</p><div class="suggestion-foot"><span>{{ item.confidence * 100 | number:'1.0-0' }}% confidence</span><button class="reject" (click)="decide('reorder-suggestions', item.id, 'REJECTED')">Reject</button><button class="approve" (click)="decide('reorder-suggestions', item.id, 'ACCEPTED')">Approve stock</button></div></div></div><ng-template #empty><div class="empty"><strong>The queue is clear.</strong><span>Simulate a sale or request a suggestion. New recommendations appear automatically.</span></div></ng-template></section>
</main>`, styles: [`main{max-width:1180px;margin:auto;padding:28px 28px 80px}header,.toolbar,.product-top,.suggestion-head,.suggestion-foot,.actions{display:flex;align-items:center;justify-content:space-between}.brand{display:flex;gap:11px;align-items:center}.brand>span{background:#e45836;color:white;font-family:'Space Grotesk';font-weight:700;padding:10px;border-radius:8px}.brand strong,.brand small{display:block}.brand strong{font-family:'Space Grotesk';font-size:19px}.brand small{color:#748078;font-size:11px;margin-top:2px}.live{font-size:11px;letter-spacing:1.5px;font-weight:700;color:#35705f}.live i{display:inline-block;width:7px;height:7px;border-radius:50%;background:#45b58d;margin-right:7px}.intro{padding:86px 0 56px}.eyebrow{font-size:11px;letter-spacing:2px;color:#e45836;font-weight:700}.intro h1{font:700 clamp(42px,7vw,82px)/.98 'Space Grotesk';letter-spacing:-2px;margin:18px 0}.intro h1 em{color:#e45836;font-style:normal}.lede,.toolbar p{color:#748078;margin:0}.toolbar{border-bottom:1px solid #d9e0d6;padding-bottom:18px}.toolbar h2,.review h2{font:600 24px 'Space Grotesk';margin:0 0 5px}.primary,.approve{border:0;background:#e45836;color:#fff;padding:12px 17px;border-radius:5px;font-weight:700;cursor:pointer}.create{display:flex;gap:8px;padding:18px 0;flex-wrap:wrap}.create input{border:1px solid #d2dbd0;padding:11px;border-radius:4px;min-width:130px;background:#fff}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:14px;padding:22px 0 66px}.product,.suggestion{background:#fff;border:1px solid #dbe3d8;border-radius:7px;padding:20px}.sku,.category,.product small,.metrics small{font-size:10px;letter-spacing:1px;color:#819087}.product h3{font:600 20px 'Space Grotesk';margin:8px 0}.status,.type,.trigger{font-size:10px;font-weight:700;letter-spacing:1px}.status{background:#e4f3e9;color:#317456;padding:6px 8px;border-radius:3px}.status.alert{background:#fff0dc;color:#bd6b21}.metrics{display:grid;grid-template-columns:repeat(3,1fr);border-top:1px solid #edf0ea;border-bottom:1px solid #edf0ea;margin:20px 0;padding:15px 0;gap:8px}.metrics strong{display:block;font:600 19px 'Space Grotesk';margin-top:6px}.actions{justify-content:flex-start;gap:8px}.actions button,.refresh,.reject{border:1px solid #d1dad0;background:transparent;padding:8px 11px;border-radius:4px;cursor:pointer;color:#355148;font-weight:600}.review{border-top:1px solid #d9e0d6;padding-top:24px}.refresh{font-size:12px}.suggestions{display:grid;gap:12px;margin-top:20px}.suggestion-head{justify-content:flex-start;gap:10px}.type{padding:5px 7px;border-radius:3px}.pricing-type{background:#fff0dc;color:#bb671e}.reorder-type{background:#e3eef8;color:#34709c}.trigger{color:#818c85}.suggestion h3{font:600 20px 'Space Grotesk';margin:18px 0 8px}.suggestion h3 b{color:#e45836}.suggestion p{color:#607068;max-width:760px;line-height:1.5}.suggestion-foot{justify-content:flex-start;gap:8px;margin-top:18px;font-size:12px;color:#758079}.suggestion-foot span{margin-right:auto}.approve{padding:9px 12px}.reject{padding:9px 12px}.empty{text-align:center;padding:50px;background:#e8eee6;border-radius:7px;color:#66746c}.empty strong,.empty span{display:block}.empty span{margin-top:8px;font-size:13px}@media(max-width:600px){main{padding:20px 16px}.intro{padding:60px 0 40px}.intro h1{letter-spacing:-1px}.live{display:none}.suggestion-foot{flex-wrap:wrap}.suggestion-foot span{flex-basis:100%;margin-bottom:8px}}`]
})
export class AppComponent implements OnDestroy {
  private http = inject(HttpClient);
  private refreshTimer: number;
  private refreshing = false;
  products: Product[] = [];
  pricing: Pricing[] = [];
  reorder: Reorder[] = [];
  strategyMode = 'RULE';
  aiConfigured = false;
  strategyAvailable = true;
  today = new Date();
  showCreate = false;
  loading = false;
  busy = false;
  error = '';
  draft = { name: '', sku: '', category: 'UNSPECIFIED', currentPrice: 25, stockLevel: 20, reorderThreshold: 10, demandVelocity: 2 };

  constructor() {
    this.load();
    this.refreshTimer = window.setInterval(() => this.load(false), 2500);
  }

  ngOnDestroy() { window.clearInterval(this.refreshTimer); }

  load(showLoading = true) {
    if (this.refreshing) return;
    this.refreshing = true;
    if (showLoading) this.loading = true;
    forkJoin({
      products: this.http.get<Product[]>('/api/products'),
      pricing: this.http.get<Pricing[]>('/api/pricing-suggestions'),
      reorder: this.http.get<Reorder[]>('/api/reorder-suggestions'),
      strategy: this.strategyAvailable
        ? this.http.get<Strategy>('/api/strategy').pipe(catchError(() => {
            this.strategyAvailable = false;
            return of({ mode: 'RULE', aiConfigured: false });
          }))
        : of({ mode: 'RULE', aiConfigured: false })
    }).subscribe({
      next: ({ products, pricing, reorder, strategy }) => {
        this.products = products;
        this.pricing = pricing;
        this.reorder = reorder;
        this.strategyMode = strategy.mode;
        this.aiConfigured = strategy.aiConfigured;
        this.error = '';
        this.refreshing = false;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.refreshing = false;
        this.loading = false;
        this.showError(error);
      }
    });
  }

  private showError(error: HttpErrorResponse) {
    this.error = error.error?.message || (error.status === 0
      ? 'The backend could not be reached. Start Spring Boot on port 8080.'
      : `Request failed (${error.status}). Please try again.`);
  }

  private runAction(request: Observable<unknown>, onSuccess?: () => void) {
    if (this.busy) return;
    this.busy = true;
    this.error = '';
    request.subscribe({
      next: () => { this.busy = false; onSuccess?.(); this.load(); },
      error: (error: HttpErrorResponse) => { this.busy = false; this.showError(error); }
    });
  }

  createProduct() {
    this.runAction(this.http.post('/api/products', this.draft), () => {
      this.showCreate = false;
      this.draft = { name: '', sku: '', category: 'UNSPECIFIED', currentPrice: 25, stockLevel: 20, reorderThreshold: 10, demandVelocity: 2 };
    });
  }

  order(product: Product) { this.runAction(this.http.post(`/api/products/${product.id}/orders`, { quantity: 1 })); }
  manual(product: Product) { this.runAction(this.http.post(`/api/products/${product.id}/suggest-pricing`, {})); }
  manualReorder(product: Product) { this.runAction(this.http.post(`/api/products/${product.id}/suggest-reorder`, {})); }
  changeStrategy(mode: string) { this.runAction(this.http.put('/api/strategy', { mode })); }
  remove(product: Product) {
    if (!window.confirm(`Remove ${product.name}? Its suggestion history will also be removed.`)) return;
    this.runAction(this.http.delete(`/api/products/${product.id}`));
  }
  decide(type: string, id: string, status: string) { this.runAction(this.http.patch(`/api/${type}/${id}`, { status })); }
  productName(id: string) { return this.products.find(product => product.id === id)?.name || 'Product'; }
}
