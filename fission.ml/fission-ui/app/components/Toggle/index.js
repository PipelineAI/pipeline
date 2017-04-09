/**
*
* LocaleToggle
*
*/

import React from 'react';

import Option from './option';

function Toggle(props) {
  let content = (<option>--</option>);

  // If we have items, render them
  if (props.values) {
    content = props.values.map((value) => (
      <Option key={value} value={value} message={props.messages[value]} />
    ));
  }

  return (
    <div className="form-inline">
      <select className="form-control" value={props.value} onChange={props.onToggle}>
        {content}
      </select>
    </div>
  );
}

Toggle.propTypes = {
  onToggle: React.PropTypes.func,
  values: React.PropTypes.array,
  value: React.PropTypes.string,
  messages: React.PropTypes.object,
};

export default Toggle;
