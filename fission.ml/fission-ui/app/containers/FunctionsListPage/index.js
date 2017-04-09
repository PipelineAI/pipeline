/*
 *
 * FunctionsListPage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { Link } from 'react-router';
import { FormattedMessage } from 'react-intl';
import { createStructuredSelector } from 'reselect';
import { makeSelectFunctions, makeSelectError, makeSelectLoading } from 'containers/FunctionsPage/selectors';
import FunctionsList from 'components/FunctionsList';
import commonMessages from 'messages';
import { loadFunctionAction, loadTriggersHttpAction, deleteFunctionAction, loadKubeWatchersAction } from './actions';

export class FunctionsListPage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor() {
    super();
    this.state = {
      filter: '',
      sorting: {
        field: '',
        ascend: true,
      },
    };

    this.makeSorterField = (field, ascend) =>
      ascend ? (a, b) => a[field] > b[field] : (a, b) => a[field] < b[field];

    this.onRemove = this.onRemove.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onChangeSortField = this.onChangeSortField.bind(this);
  }

  componentDidMount() {
    this.props.loadFunctionsData(); // TODO need improvement, maybe fork in sagas.js
    this.props.loadTriggersHttpData();
    this.props.loadKubeWatchersData();
  }

  onRemove(item) {
    const { deleteFunction } = this.props;
    deleteFunction(item);
  }

  onChange(e) {
    const obj = {};
    obj[e.target.name] = e.target.value;
    this.setState(obj);
  }

  onChangeSortField(name) {
    const { sorting } = this.state;
    sorting.ascend = sorting.field !== name ? true : !sorting.ascend;
    sorting.field = name;
    this.setState({ sorting });
  }

  render() {
    const { loading, error } = this.props;
    let { items } = this.props;
    const { filter, sorting } = this.state;

    items = filter === '' ? items : items.filter((item) => {
      if (item.name.includes(filter) || item.environment.includes(filter)) {
        return true;
      }
      let stay = false;
      item.triggersHttp.forEach((t) => {
        stay = stay || t.method.includes(filter) || t.urlpattern.includes(filter);
      });
      item.kubeWatchers.forEach((w) => {
        stay = stay || w.namespace.includes(filter) || w.objtype.includes(filter) || w.labelselector.includes(filter);
      });
      return stay;
    });

    items = sorting.field === '' ? items : items.sort(this.makeSorterField(sorting.field, sorting.ascend));

    const functionsListProps = {
      loading,
      error,
      items,
    };

    return (
      <div>
        <Helmet
          title="List functions"
        />
        <div className="form-group form-inline">
          <label htmlFor="functionListFilter"><FormattedMessage {...commonMessages.filter} /></label>
          <input type="text" className="form-control" name="filter" onChange={this.onChange} value={filter} />
          <Link to="/functions/batch_upload" className="pull-right btn btn-info"><FormattedMessage {...commonMessages.batchUpload} /></Link>
          <Link to="/functions/create" className="pull-right btn btn-primary"><FormattedMessage {...commonMessages.add} /></Link>
        </div>
        <FunctionsList {...functionsListProps} onRemove={this.onRemove} onChangeSortField={this.onChangeSortField} />
      </div>
    );
  }
}

FunctionsListPage.propTypes = {
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
  items: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  loadFunctionsData: PropTypes.func,
  loadTriggersHttpData: PropTypes.func,
  loadKubeWatchersData: PropTypes.func,
  deleteFunction: PropTypes.func,
};

const mapStateToProps = createStructuredSelector({
  items: makeSelectFunctions(),
  loading: makeSelectLoading(),
  error: makeSelectError(),
});

function mapDispatchToProps(dispatch) {
  return {
    loadFunctionsData: () => dispatch(loadFunctionAction()),
    loadTriggersHttpData: () => dispatch(loadTriggersHttpAction()),
    loadKubeWatchersData: () => dispatch(loadKubeWatchersAction()),
    deleteFunction: (func) => dispatch(deleteFunctionAction(func)),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(FunctionsListPage);
