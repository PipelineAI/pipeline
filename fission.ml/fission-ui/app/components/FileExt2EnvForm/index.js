import React, { PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

class FileExt2EnvForm extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { environments, fileExt2Env, draftMapping, onDraftMappingChange, onDraftMappingCreate, onRemoveFileExtMapping } = this.props;
    return (
      <div>
        <h3><FormattedMessage {...commonMessages.fileExt} /></h3>
        <table className="table table-bordered">
          <thead>
            <tr>
              <th><FormattedMessage {...commonMessages.fileExt} /></th>
              <th><FormattedMessage {...commonMessages.environment} /></th>
              <th><FormattedMessage {...commonMessages.action} /></th>
            </tr>
          </thead>
          <tbody>
            {
              Object.keys(fileExt2Env).map((k, idx) =>
                <tr key={`fileext2env-${idx}`}>
                  <th>{k}</th>
                  <th>{fileExt2Env[k]}</th>
                  <th>
                    <a onClick={() => onRemoveFileExtMapping(k)} className="btn btn-danger"><FormattedMessage {...commonMessages.delete} /></a>
                  </th>
                </tr>
              )
            }
          </tbody>
        </table>
        <form className="form-inline">
          <div className="form-group">
            <label htmlFor="extensionInput"><FormattedMessage {...commonMessages.fileExt} /></label>
            <input className="form-control" name="extension" value={draftMapping.extension} onChange={onDraftMappingChange} />
          </div>
          <div className="form-group">
            <label htmlFor="environmentInput"><FormattedMessage {...commonMessages.environment} /></label>
            <select className="form-control" name="environment" value={draftMapping.environment} onChange={onDraftMappingChange} >
              <option key={'environment-0'} />
              {
                environments.map((e, idx) =>
                  <option key={`environment-${idx + 1}`} value={e.name} >{e.name}</option>
                )
              }
            </select>
          </div>
          <a onClick={onDraftMappingCreate} className="btn btn-primary"><FormattedMessage {...commonMessages.add} /></a>
        </form>
      </div>
    );
  }
}

FileExt2EnvForm.propTypes = {
  fileExt2Env: PropTypes.object,
  draftMapping: PropTypes.object,
  onRemoveFileExtMapping: PropTypes.func.isRequired,
  onDraftMappingChange: PropTypes.func.isRequired,
  onDraftMappingCreate: PropTypes.func.isRequired,
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
};

export default FileExt2EnvForm;
