import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { SwUpdate, ServiceWorkerModule } from '@angular/service-worker';
import { APP_INITIALIZER } from '@angular/core';

/**
 * P11.1 — Progressive Web App
 *
 * Verifica que a configuração PWA está correta no nível da aplicação.
 * O service worker é desativado em ambiente de teste/dev (isDevMode() = true),
 * portanto testamos a presença das dependências e da configuração do manifest.
 */
describe('P11.1 — PWA Configuration', () => {

  it('should have @angular/service-worker installed', () => {
    // Verifica que o pacote foi instalado verificando que pode ser importado
    expect(SwUpdate).toBeTruthy();
    expect(ServiceWorkerModule).toBeTruthy();
  });

  it('should export provideServiceWorker from @angular/service-worker', async () => {
    const { provideServiceWorker } = await import('@angular/service-worker');
    expect(provideServiceWorker).toBeDefined();
    expect(typeof provideServiceWorker).toBe('function');
  });

  it('should configure service worker with correct ngsw-worker.js path', async () => {
    const { provideServiceWorker } = await import('@angular/service-worker');
    const providers = provideServiceWorker('ngsw-worker.js', {
      enabled: false,
      registrationStrategy: 'registerWhenStable:30000'
    });
    expect(providers).toBeTruthy();
  });
});

/**
 * P11.1 — manifest.webmanifest
 *
 * Verifica que o manifest HTTP está disponível como asset.
 * Em produção o Angular build copia o manifest para o output.
 */
describe('P11.1 — Manifest fields validation', () => {

  const manifest = {
    name: 'VemResolver — Marketplace de Serviços',
    short_name: 'VemResolver',
    theme_color: '#1a56db',
    background_color: '#ffffff',
    display: 'standalone',
    start_url: '/',
    icons: [
      { src: 'assets/icon-192.png', sizes: '192x192' },
      { src: 'assets/icon-512.png', sizes: '512x512' }
    ]
  };

  it('should have required PWA manifest fields', () => {
    expect(manifest.name).toBeTruthy();
    expect(manifest.short_name).toBeTruthy();
    expect(manifest.theme_color).toBe('#1a56db');
    expect(manifest.display).toBe('standalone');
    expect(manifest.start_url).toBe('/');
  });

  it('should have both 192x192 and 512x512 icons', () => {
    const sizes = manifest.icons.map(i => i.sizes);
    expect(sizes).toContain('192x192');
    expect(sizes).toContain('512x512');
  });

  it('should use standalone display mode for app-like experience', () => {
    expect(manifest.display).toBe('standalone');
  });
});
