/**
*
* ToggleOption
*
*/

import React from 'react';
import { injectIntl, intlShape } from 'react-intl';

const Option = ({ value, message, intl }) => (
  <option value={value}>
    {message ? intl.formatMessage(message) : value}
  </option>
);

Option.propTypes = {
  value: React.PropTypes.string.isRequired,
  message: React.PropTypes.object,
  intl: intlShape.isRequired,
};

export default injectIntl(Option);
