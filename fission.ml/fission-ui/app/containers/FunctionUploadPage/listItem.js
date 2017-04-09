/*
 *
 * FunctionUploadListItem
 *
 */

import React, { PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import { Link } from 'react-router';
import commonMessages from 'messages';

export class ListItem extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { item, onRemove } = this.props;
    return (
      <tr>
        <td>{ item.name }</td>
        <td><Link to={`/environments/edit/${item.environment}`}>{ item.environment }</Link></td>
        <td>
          <FormattedMessage {...commonMessages[item.status]} />
          { item.errors.length > 0 &&
            (<ul>
              {
                item.errors.map((e, idx) => <li key={`error-${idx}`}>{e}</li>)
              }
            </ul>)
          }
        </td>
        <td>
          {
            item.status === 'uploaded' &&
            <Link className="btn btn-primary" to={`/functions/edit/${item.name}`}><FormattedMessage {...commonMessages.edit} /></Link>
          }
          <a onClick={onRemove} className="btn btn-danger"><FormattedMessage {...commonMessages.delete} /></a>
        </td>
      </tr>
    );
  }
}

ListItem.propTypes = {
  item: PropTypes.object,
  onRemove: PropTypes.func,
};

export default ListItem;
