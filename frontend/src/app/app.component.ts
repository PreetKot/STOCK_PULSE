import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, Observable, of } from 'rxjs';

type Product = { id: string; sku: string; name: string; category: string; currentPrice: number; stockLevel: number; reorderThreshold: number; demandVelocity: number; status: string };
type Pricing = { id: string; productId: string; recommendedPrice: number; direction: string; confidence: number; reasoning: string; status: string; triggerReason: string };
type Reorder = { id: string; productId: string; recommendedQuantity: number; confidence: number; reasoning: string; status: string; triggerReason: string };
type Strategy = { mode: string; aiConfigured: boolean };

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
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
    console.error('StockPulse request failed', error);
  }

  private runAction(request: Observable<unknown>, onSuccess?: () => void) {
    if (this.busy) return;
    this.busy = true;
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
  get pendingCount() { return this.pricing.length + this.reorder.length; }
  get lowStockCount() { return this.products.filter(product => product.stockLevel <= product.reorderThreshold).length; }
  get healthyCount() { return this.products.filter(product => product.stockLevel > product.reorderThreshold).length; }
  stockPercent(product: Product) {
    const target = Math.max(product.reorderThreshold * 3, 1);
    return Math.min(100, Math.max(4, (product.stockLevel / target) * 100));
  }
  categoryMark(category: string) {
    return category === 'ELECTRONICS' ? '⌁' : category === 'APPAREL' ? '✦' : category === 'HOME' ? '⌂' : '•';
  }
}
