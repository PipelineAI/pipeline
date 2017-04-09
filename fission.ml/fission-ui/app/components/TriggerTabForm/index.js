/**
*
* TriggerTabForm
*
*/

import React from 'react';
// import styled from 'styled-components';
import TriggerHttpForm from 'components/TriggerHttpForm';
import KubeWatcherForm from 'components/KubeWatcherForm';

class TriggerTabForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { triggers, onHttpTriggerCreate, onHttpTriggerRemove, onKubeWatcherCreate, onKubeWatcherRemove } = this.props;

    return (
      <div>
        <TriggerHttpForm triggers={triggers.triggersHttp} onRemove={onHttpTriggerRemove} onCreate={onHttpTriggerCreate} />
        <KubeWatcherForm watchers={triggers.kubeWatchers} onRemove={onKubeWatcherRemove} onCreate={onKubeWatcherCreate} />
      </div>
    );
  }
}

TriggerTabForm.propTypes = {
  triggers: React.PropTypes.object,
  onHttpTriggerCreate: React.PropTypes.func,
  onHttpTriggerRemove: React.PropTypes.func,
  onKubeWatcherCreate: React.PropTypes.func,
  onKubeWatcherRemove: React.PropTypes.func,
};

export default TriggerTabForm;
