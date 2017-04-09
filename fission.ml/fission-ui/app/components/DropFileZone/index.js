import React, { PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import commonMessages from 'messages';

class DropFileZone extends React.Component { // eslint-disable-line react/prefer-stateless-function

  constructor(props) {
    super(props);
    this.onDrop = this.onDrop.bind(this);
  }

  onDragOver(e) {
    const evt = e;
    evt.stopPropagation();
    evt.preventDefault();
    evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
  }

  onDrop(evt) {
    evt.stopPropagation();
    evt.preventDefault();

    const files = evt.dataTransfer.files; // FileList object.
    // files is a FileList of File objects. List some properties.
    this.props.onFilesDropped(Array.from(files));
  }

  style = {
    border: '2px dashed #bbb',
    borderRadius: '5px',
    padding: '25px',
    textAlign: 'center',
    color: '#bbb',
  };

  render() {
    return (
      <div style={this.style} onDragOver={this.onDragOver} onDrop={this.onDrop} >
        <FormattedMessage {...commonMessages.dropFilesHere} />
      </div>
    );
  }
}

DropFileZone.propTypes = {
  onFilesDropped: PropTypes.func.isRequired,
};

export default DropFileZone;
