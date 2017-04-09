/**
*
* ErrorIndicator
*
*/

import React, { PropTypes } from 'react';

function ErrorIndicator({ errors }) {
  return (
    <div className="alert alert-danger" >
      <ul>
        {errors.map((e, i) => <li key={`error-item${i}`}>{e}</li>)}
      </ul>
    </div>
  );
}

ErrorIndicator.propTypes = {
  errors: PropTypes.array,
};

export default ErrorIndicator;
