/**
*
* TriggerHttpItemForm
*
*/

import React from 'react';
import ReactTooltip from 'react-tooltip';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';
// import styled from 'styled-components';


class Item extends React.Component { // eslint-disable-line react/prefer-stateless-function

  makeCopy(endpoint) {
    return () => {
      window.prompt('Copy to clipboard: Ctrl+C, Enter', endpoint);
    };
  }

  render() {
    const { trigger, onRemove } = this.props;
    const endpoint = `${window.location.origin}/proxy/router${trigger.urlpattern}`;
    return (
      <tr>
        <td>{trigger.method}</td>
        <td>{trigger.urlpattern}</td>
        <td>
          <a className="btn btn-danger" onClick={onRemove}><FormattedMessage {...commonMessages.delete} /></a> { ' ' }
          <a className="btn btn-default" data-tip={endpoint} onClick={this.makeCopy(endpoint)}><FormattedMessage {...commonMessages.copyEndpoint} /></a>
          <ReactTooltip />
        </td>
      </tr>
    );
  }
}

Item.propTypes = {
  trigger: React.PropTypes.object,
  onRemove: React.PropTypes.func,
};

export default Item;
