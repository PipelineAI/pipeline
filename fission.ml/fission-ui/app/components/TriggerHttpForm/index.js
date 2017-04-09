/**
*
* TriggerHttpForm
*
*/

import React from 'react';
// import styled from 'styled-components';
import TriggerHttpCreateForm from 'components/TriggerHttpCreateForm';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';
import messages from './messages';
import Item from './item';

class TriggerHttpForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { triggers, onRemove, onCreate } = this.props;
    return (
      <div>
        <h3><FormattedMessage {...messages.headerhttptrigger} /></h3>
        <table className="table table-bordered">
          <thead>
            <tr>
              <th><FormattedMessage {...commonMessages.method} /></th>
              <th><FormattedMessage {...commonMessages.path} /></th>
              <th><FormattedMessage {...commonMessages.action} /></th>
            </tr>
          </thead>
          <tbody>
            {
              triggers.map((item, index) => (
                <Item trigger={item} key={`triggers-${index}`} onRemove={() => { onRemove(item); }} />
              ))
            }
          </tbody>
        </table>
        <TriggerHttpCreateForm onCreate={onCreate} />
      </div>
    );
  }
}

TriggerHttpForm.propTypes = {
  triggers: React.PropTypes.array,
  onRemove: React.PropTypes.func,
  onCreate: React.PropTypes.func,
};

export default TriggerHttpForm;
