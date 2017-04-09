/**
*
* RequestBodyBuilder
*
*/

import React from 'react';
import AceEditor from 'react-ace';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/plain_text';
import 'brace/theme/monokai';

class RequestBodyBuilder extends React.Component { // eslint-disable-line react/prefer-stateless-function
  render() {
    const { bodytype, content, onSelectType, onContentChange } = this.props;
    return (
      <div>
        <select className="form-control" name="bodytype" value={bodytype} onChange={onSelectType}>
          <option value="plain_text">Text</option>
          <option value="json">Json</option>
          <option value="xml">Xml</option>
        </select>
        <AceEditor
          mode={bodytype}
          theme="monokai"
          name="RequestBodyForm"
          value={content}
          editorProps={{ $blockScrolling: true }}
          onChange={onContentChange}
          height="150px"
          width="100%"
        />
      </div>
    );
  }
}

RequestBodyBuilder.propTypes = {
  bodytype: React.PropTypes.string.isRequired,
  content: React.PropTypes.string.isRequired,
  onSelectType: React.PropTypes.func.isRequired,
  onContentChange: React.PropTypes.func.isRequired,
};

export default RequestBodyBuilder;
