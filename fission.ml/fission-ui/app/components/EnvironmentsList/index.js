/**
*
* EnvironmentsList
*
*/

import React, { PropTypes } from 'react';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';
import Item from './item';

// import styled from 'styled-components';

function EnvironmentsList({ loading, error, environments, onRemove }) {
  if (loading) {
    return <LoadingIndicator />;
  }
  if (error !== false) {
    return <ErrorIndicator errors={[error.response.data]} />;
  }
  return (
    <table className="table table-bordered">
      <thead>
        <tr>
          <th><FormattedMessage {...commonMessages.environmentName} /></th>
          <th><FormattedMessage {...commonMessages.dockerImage} /></th>
          <th><FormattedMessage {...commonMessages.action} /></th>
        </tr>
      </thead>
      <tbody>
        {
          environments.map((item, index) => (
            <Item item={item} key={`environment-${index}`} onRemove={() => { onRemove(item); }} />
          ))
        }
      </tbody>
    </table>
  );
}

EnvironmentsList.propTypes = {
  loading: PropTypes.bool,
  error: PropTypes.any,
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  onRemove: PropTypes.func,
};

export default EnvironmentsList;
