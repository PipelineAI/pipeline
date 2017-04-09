/**
*
* KeyValueRow
*
*/

import React from 'react';

class Row extends React.Component {
  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
  }

  onChange(e) {
    const pair = [this.props.thekey, this.props.thevalue];
    pair[e.target.name] = e.target.value;
    this.props.onChange(this.props.index, pair);
  }

  render() {
    const { index, thekey, thevalue } = this.props;
    return (
      <div className="row">
        <div className="col-md-4">
          <input placeholder="key" type="text" className="form-control input-sm" value={thekey} name={0} onChange={this.onChange} />
        </div>
        <div className="col-md-4">
          <input placeholder="value" type="text" className="form-control input-sm" value={thevalue} name={1} onChange={this.onChange} />
        </div>
        <div className="col-md-4">
          <a className="btn btn-danger" onClick={() => this.props.onDelete(index)} >x</a>
        </div>
      </div>
    );
  }
}

Row.propTypes = {
  index: React.PropTypes.number.isRequired,
  thekey: React.PropTypes.string.isRequired,
  thevalue: React.PropTypes.string.isRequired,
  onChange: React.PropTypes.func.isRequired,
  onDelete: React.PropTypes.func.isRequired,
};

export default Row;
