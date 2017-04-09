/**
*
* FunctionForm
*
*/

import React, { PropTypes } from 'react';
// import styled from 'styled-components';
import FunctionTabForm from 'components/FunctionTabForm';
import TriggerTabForm from 'components/TriggerTabForm';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

class FunctionForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);

    this.state = {
      activeTab: props.activeTab,
      environments: props.environments,
      item: props.item,
    };
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.environments.length !== this.state.environments.length) {
      this.state.environments = nextProps.environments;
    }
    if (nextProps.item.length !== this.state.item) {
      this.state.item = nextProps.item;
    }
    if (nextProps.activeTab !== this.state.activeTab) {
      this.state.activeTab = nextProps.activeTab;
    }
  }


  render() {
    const { item, activeTab, environments } = this.state;
    const { onChange, onHttpTriggerRemove, onHttpTriggerCreate, onKubeWatcherRemove, onKubeWatcherCreate, metadataEditable, onCodeChange, onTabChange, onFunctionTest, functionTest } = this.props;

    return (
      <div>
        <ul className="nav nav-tabs">
          <li role="presentation" className={activeTab === 'function' ? 'active' : ''}><a onClick={() => onTabChange('function')}><FormattedMessage {...commonMessages.function} /></a></li>
          <li role="presentation" className={activeTab === 'trigger' ? 'active' : ''}><a onClick={() => onTabChange('trigger')}><FormattedMessage {...commonMessages.trigger} /></a></li>
        </ul>
        <div style={{ display: activeTab === 'function' ? 'block' : 'none' }}>
          <FunctionTabForm item={item} environments={environments} onChange={onChange} metadataEditable={metadataEditable} onCodeChange={onCodeChange} onFunctionTest={onFunctionTest} functionTest={functionTest} />
        </div>
        <div style={{ display: activeTab === 'trigger' ? 'block' : 'none' }}>
          <TriggerTabForm
            triggers={{ triggersHttp: item.triggersHttp, kubeWatchers: item.kubeWatchers }}
            onHttpTriggerRemove={onHttpTriggerRemove} onHttpTriggerCreate={onHttpTriggerCreate}
            onKubeWatcherRemove={onKubeWatcherRemove} onKubeWatcherCreate={onKubeWatcherCreate}
          />
        </div>
      </div>
    );
  }
}

FunctionForm.propTypes = {
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  item: PropTypes.object,
  onChange: PropTypes.func,
  onHttpTriggerRemove: PropTypes.func,
  onHttpTriggerCreate: PropTypes.func,
  onKubeWatcherRemove: PropTypes.func,
  onKubeWatcherCreate: PropTypes.func,
  metadataEditable: PropTypes.bool,
  onCodeChange: PropTypes.func,
  onTabChange: PropTypes.func,
  onFunctionTest: PropTypes.func,
  activeTab: PropTypes.string,
  functionTest: PropTypes.object,
};

export default FunctionForm;
