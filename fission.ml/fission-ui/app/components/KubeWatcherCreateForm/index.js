/**
*
* KubeWatcherCreateForm
*
*/

import React from 'react';
// import styled from 'styled-components';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

class KubeWatcherCreateForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);

    this.state = {
      namespace: 'default',
      objtype: 'pod',
      labelselector: '',
    };
    this.onChange = this.onChange.bind(this);
    this.onWatcherCreate = this.onWatcherCreate.bind(this);
  }

  onChange(event) {
    const target = event.target;
    this.state[target.name] = target.value;
  }

  onWatcherCreate(event) {
    event.preventDefault();
    const { onCreate } = this.props;
    onCreate(this.state);
  }

  render() {
    const { namespace, objtype, labelselector } = this.state;
    return (
      <form className="form-inline">
        <div className="form-group">
          <label htmlFor="watcherCreateNamespace"><FormattedMessage {...commonMessages.namespace} /></label>
          <input type="text" className="form-control" id="watcherCreateNamespace" defaultValue={namespace} name="namespace" onChange={this.onChange} />
        </div>

        <div className="form-group">
          <label htmlFor="watcherCreateObjType"><FormattedMessage {...commonMessages.objtype} /></label>
          <input type="text" className="form-control" id="watcherCreateObjType" defaultValue={objtype} name="objtype" onChange={this.onChange} />
        </div>

        <div className="form-group">
          <label htmlFor="watcherCreateLabelSelector"><FormattedMessage {...commonMessages.labelselector} /></label>
          <input type="text" className="form-control" id="watcherCreateLabelSelector" defaultValue={labelselector} name="labelselector" onChange={this.onChange} />
        </div>
        <button className="btn btn-default" onClick={this.onWatcherCreate} ><FormattedMessage {...commonMessages.add} /></button>
      </form>
    );
  }
}

KubeWatcherCreateForm.propTypes = {
  onCreate: React.PropTypes.func,
};

export default KubeWatcherCreateForm;
