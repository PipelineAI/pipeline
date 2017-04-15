/**
*
* FunctionsList
*
*/

import React, { PropTypes } from 'react';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import FunctionListItem from 'containers/FunctionListItem';
// import styled from 'styled-components';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

function FunctionsList({ loading, error, items, onRemove, onChangeSortField }) {
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
          <th><a onClick={() => onChangeSortField('name')}><FormattedMessage {...commonMessages.name} /></a></th>
          <th><a onClick={() => onChangeSortField('environment')}><FormattedMessage {...commonMessages.environment} /></a></th>
          <th><FormattedMessage {...commonMessages.trigger} /></th>
          <th><FormattedMessage {...commonMessages.action} /></th>
        </tr>
      </thead>
      <tbody>
        {
          items.map((item, index) => (
            <FunctionListItem item={item} key={`function-${index}`} onRemove={() => { onRemove(item); }} />
          ))
        }
      </tbody>
    </table>
  );
}

FunctionsList.propTypes = {
  loading: PropTypes.bool,
  error: PropTypes.any,
  items: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  onRemove: PropTypes.func,
  onChangeSortField: PropTypes.func,
};

export default FunctionsList;
