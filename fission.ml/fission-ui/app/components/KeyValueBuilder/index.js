/**
*
* KeyValueBuilder
*
*/

import React from 'react';
import Row from './row';

class KeyValueBuilder extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);
    this.state = {
      pairs: this.extractData(props.defaultValue),
    };
    this.onChange = this.onChange.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.createNewRow = this.createNewRow.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const pairs = this.extractData(nextProps.defaultValue);
    this.setState({ pairs });
  }

  onChange(index, pair) {
    const { pairs } = this.state;
    pairs[index] = pair;
    const dict = {};
    pairs.map((p) => { dict[p[0]] = p[1]; return p; });
    this.props.onChange({
      target: {
        name: this.props.name,
        value: dict,
      },
    });
  }

  onDelete(index) {
    const { pairs } = this.state;
    pairs.splice(index, 1);
    this.setState({ pairs });
  }

  extractData(defaultValue) {
    let pairs = [['', '']];
    if (Object.keys(defaultValue).length > 0) {
      pairs = Object.keys(defaultValue).map((key) => [key, defaultValue[key]]);
    }
    return pairs;
  }

  createNewRow() {
    const { pairs } = this.state;
    pairs.push(['', '']);
    this.setState({ pairs });
  }

  render() {
    const { pairs } = this.state;
    return (
      <div>
        {pairs.map((pair, index) => <Row key={`pair-${index}`} index={index} thekey={pair[0]} thevalue={pair[1]} onChange={this.onChange} onDelete={this.onDelete} />)}
        <a className="btn btn-default" onClick={this.createNewRow}>+</a>
      </div>
    );
  }
}

KeyValueBuilder.propTypes = {
  name: React.PropTypes.string.isRequired,
  defaultValue: React.PropTypes.object.isRequired,
  onChange: React.PropTypes.func.isRequired,
};
export default KeyValueBuilder;
