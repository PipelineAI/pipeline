/**
*
* EnvironmentForm
*
*/

import React from 'react';
import { Link } from 'react-router';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';
// import styled from 'styled-components';

class EnvironmentForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { environment, onSave, onChange, nameEditable, onSelectSample, sampleEnabled } = this.props;

    return (
      <form>
        <div className="form-group">
          <label htmlFor="formEnvironmentName"><FormattedMessage {...commonMessages.name} /></label>
          { nameEditable ? (
            <input type="text" className="form-control" id="formEnvironmentName" name="name" value={environment.name} onChange={onChange} />
          ) : (
            <input type="text" className="form-control" id="formEnvironmentName" readOnly name="name" value={environment.name} onChange={onChange} />
          )
          }
        </div>
        <div className="form-group">
          <label htmlFor="formEnvironmentImage"><FormattedMessage {...commonMessages.dockerImage} /></label>
          <input type="text" className="form-control" id="formEnvironmentImage" name="image" value={environment.image} onChange={onChange} />
        </div>
        { sampleEnabled &&
          <div className="form-group">
            <label htmlFor="formEnvironmentSample"><FormattedMessage {...commonMessages.chooseSample} /></label>
            <select className="form-control" onChange={onSelectSample} disabled={!nameEditable}>
              <option value="blank" />
              <option value="node">Node</option>
              <option value="python">Python</option>
            </select>
          </div>
        }
        <a className="btn btn-primary" onClick={onSave}><FormattedMessage {...commonMessages.save} /></a> { ' ' }
        <Link to="/environments" className="btn btn-default"><FormattedMessage {...commonMessages.cancel} /></Link>

      </form>
    );
  }
}

EnvironmentForm.propTypes = {
  environment: React.PropTypes.object,
  onSave: React.PropTypes.func,
  onChange: React.PropTypes.func.isRequired,
  nameEditable: React.PropTypes.bool,
  sampleEnabled: React.PropTypes.bool,
  onSelectSample: React.PropTypes.func,
};

export default EnvironmentForm;
