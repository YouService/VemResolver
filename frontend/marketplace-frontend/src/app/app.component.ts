import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NavbarComponent } from './components/navbar.component';
import { CookieBannerComponent } from './components/cookie-banner.component';
import { filter } from 'rxjs/operators';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { GlobalSnackBarComponent } from './components/global-snack-bar/global-snack-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CookieBannerComponent, MatSnackBarModule, MatIconModule, GlobalSnackBarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  showNavbar = true;

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const hiddenRoutes = ['/termos-uso', '/politica-privacidade', '/seja-um-profissional', '/seguranca', '/sobre-nos'];
      this.showNavbar = !hiddenRoutes.some(route => event.url.includes(route));
    });
  }
}
