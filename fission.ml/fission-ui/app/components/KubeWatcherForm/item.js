/**
*
* KubeWatcherItemForm
*
*/

import React from 'react';
// import styled from 'styled-components';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

class Item extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { watcher, onRemove } = this.props;
    return (
      <tr>
        <td>{watcher.namespace}</td>
        <td>{watcher.objtype}</td>
        <td>{watcher.labelselector}</td>
        <td><a className="btn btn-danger" onClick={onRemove}><FormattedMessage {...commonMessages.delete} /></a></td>
      </tr>
    );
  }
}

Item.propTypes = {
  watcher: React.PropTypes.object,
  onRemove: React.PropTypes.func,
};

export default Item;
