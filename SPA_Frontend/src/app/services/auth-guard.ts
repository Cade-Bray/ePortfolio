import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanMatch,
  Route,
  Router,
  RouterStateSnapshot, UrlSegment, UrlTree
} from '@angular/router';
import {Injectable} from '@angular/core';
import {Authentication} from './authentication';

@Injectable({ providedIn: 'root'})
export class AuthGuard implements CanActivate, CanMatch {
  constructor(private auth: Authentication, private router: Router) {}

  /**
   * Determine if a route can be activated based on authentication status.
   * @param _route This is the route to be activated.
   * @param state This is the current router state.
   * @return True if the route can be activated, or a UrlTree to redirect to log in.
   */
  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree{
    if (this.auth.isLoggedIn()) return true;
    return this.router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }

  /**
   * Determine if a route can be matched based on authentication status.
   * @param _route This is the route to be matched.
   * @param segments This is the URL segments of the route.
   * @return True if the route can be matched, or a UrlTree to redirect to log in.
   */
  canMatch(_route: Route, segments: UrlSegment[]): boolean | UrlTree {
    // Check if the user is logged in
    if (this.auth.isLoggedIn()) return true;
    // Reconstruct the URL from the segments
    const url = '/' + segments.map(s => s.path).join('/');
    // Redirect to log in with returnUrl
    return this.router.createUrlTree(['/login'], { queryParams: { returnUrl: url } });
  }
}
