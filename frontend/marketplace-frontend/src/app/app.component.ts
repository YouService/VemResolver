import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NavbarComponent } from './components/navbar.component';
import { CookieBannerComponent } from './components/cookie-banner.component';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CookieBannerComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  showNavbar = true;

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const hiddenRoutes = ['/termos-uso', '/politica-privacidade', '/seja-um-profissional'];
      this.showNavbar = !hiddenRoutes.some(route => event.url.includes(route));
    });
  }
}
