/**
*
* NavLink
*
*/

import React, { PureComponent } from 'react';
import { Link } from 'react-router';

class NavLink extends PureComponent { // eslint-disable-line react/prefer-stateless-function
  render() {
    const isActive = this.context.router.isActive(this.props.to, false);
    const className = isActive ? 'active' : '';
    return (
      <li className={className}>
        <Link {...this.props} />
      </li>
    );
  }
}

NavLink.contextTypes = {
  router: React.PropTypes.object,
};
NavLink.propTypes = {
  to: React.PropTypes.string,
};
export default NavLink;
