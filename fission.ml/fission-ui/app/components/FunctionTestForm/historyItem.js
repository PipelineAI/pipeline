/**
 *
 * FunctionTestHistoryItemForm
 *
 */

import React from 'react';

class historyItem extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { index, item, onSelect } = this.props;
    const { method, headers, params, bodytype } = item;
    return (
      <div>
        <a className="btn" onClick={() => onSelect(item)}>
          <span className="label-warning"># {index}</span> { ' ' }
          <span className="label-info">{method}</span> { ' ' }
          <span className="label-info">{Object.keys(headers).length} headers</span> { ' ' }
          <span className="label-info">{Object.keys(params).length} params</span> { ' ' }
          <span className="label-info">{bodytype}</span> { ' ' }
        </a>
      </div>

    );
  }
}

historyItem.propTypes = {
  index: React.PropTypes.number,
  item: React.PropTypes.object,
  onSelect: React.PropTypes.func.isRequired,
};

export default historyItem;
