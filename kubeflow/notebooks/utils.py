# Copyright 2019 Google LLC. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Utils to query ml-metadata store in a notebook."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

from IPython.display import display_html
import matplotlib.pyplot as plt
import networkx as nx
import pandas as pd

from ml_metadata.proto import metadata_store_pb2


def _is_output_event(event):
  """Checks if event is an Output event."""
  return event.type in [metadata_store_pb2.Event.DECLARED_OUTPUT,
                        metadata_store_pb2.Event.OUTPUT]


def _is_input_event(event):
  """Checks if event is an Input event."""
  return event.type in [metadata_store_pb2.Event.DECLARED_INPUT,
                        metadata_store_pb2.Event.INPUT]


def _get_value_str(p):
  """Returns a string representation of a `metadata_store_pb2.Value` object."""
  if p.int_value:
    return str(p.int_value)
  if p.string_value:
    return p.string_value
  if p.double_value:
    return str(p.double_value)
  return ''


class _LineageGraphHelper(object):
  """A helper class to compute and plot lineage of ml-metadata artifacts."""

  def __init__(self, store):
    """Initializes the _LineageGraphBuilder with given metadata store.

    Args:
      store: An instance of `metadata_store.MetadataStore`.
    """
    self.metadata_store = store

  def _get_upstream_execution_ids(self, artifact_id):
    """Returns a list of execution ids that generated `artifact_id`."""
    events = self.metadata_store.get_events_by_artifact_ids([artifact_id])
    return [e.execution_id for e in events if _is_output_event(e)]

  def _get_upstream_artifact_ids(self, execution_id):
    """Returns a list of artifact_ids that were inputs for `execution_id`."""
    events = self.metadata_store.get_events_by_execution_ids([execution_id])
    return [e.artifact_id for e in events if _is_input_event(e)]

  def _add_node_attribute(self, g, node_id, depth, is_artifact):
    """Adds the attributes of given artifact or execution to the graph `g`."""
    # if it is not an artifact, use negative gnode id
    gnode_id = node_id if is_artifact else -1 * node_id
    g.add_node(gnode_id, depth=depth, is_artifact=is_artifact)
    node_label = ''
    if is_artifact:
      [a] = self.metadata_store.get_artifacts_by_id([node_id])
      [t] = self.metadata_store.get_artifact_types_by_id([a.type_id])
      node_label += t.name
    else:
      [e] = self.metadata_store.get_executions_by_id([node_id])
      [t] = self.metadata_store.get_execution_types_by_id([e.type_id])
      node_label += t.name
    g.nodes[gnode_id]['_label_'] = node_label

  def _add_parents(self, g, node_id, is_artifact, depth, max_depth=None):
    """Adds the parent artifacts/executions for `node_id` to the graph `g`."""
    # if it is not an artifact, use negative gnode id
    gnode_id = node_id if is_artifact else -1 * node_id
    self._add_node_attribute(g, node_id, depth, is_artifact)
    if gnode_id in g and g.in_edges(gnode_id):
      return
    if max_depth is not None and depth > max_depth:
      return
    if is_artifact:
      for e_id in self._get_upstream_execution_ids(node_id):
        g.add_edge(e_id * -1, node_id)
        self._add_parents(g, e_id, not is_artifact, depth + 1, max_depth)
    else:
      for a_id in self._get_upstream_artifact_ids(node_id):
        g.add_edge(a_id, node_id * -1)
        self._add_parents(g, a_id, not is_artifact, depth + 1, max_depth)

  def get_artifact_lineage(self, artifact_id, max_depth=None):
    """Returns a `nx.DiGraph` representing the lineage of given `artifact_id`.

    Args:
      artifact_id: An `int` indicating the id of an Artifact.
      max_depth: (Optional): An `int` indicating how far back the lineage
          should be computed for `artifact_id`. By default the entire lineage
          is computed.

    Returns:
      A `nx.DiGraph` for the lineage of given `artifact_id`.
      Nodes with positive ids indicate an Artifact.
      Nodes with negative ids indicate an Execution.
    """
    g = nx.DiGraph(query_artifact_id=artifact_id)
    if max_depth is None or max_depth > 0:
      self._add_parents(g, artifact_id, True, 1, max_depth)
    return g

  def plot_artifact_lineage(self, g):
    """Plots a `nx.DiGraph` object.

    This method uses networkx and matplotlib to plot the graph.
    The nodes are places from left to right w.r.t. its depth.
    Nodes at the same depths are placed vertically.
    Artifact is shown in green, and Execution is shown in red.
    Nodes are positioned in a bipartite graph layout.

    Args:
      g: A `nx.DiGraph` object.
    """
    # make a copy of the graph; add auxiliary nodes
    dag = g.copy(as_view=False)
    label_anchor_id = 10000
    for node_id in g.nodes:
      if node_id > 0:
        dag.add_node(label_anchor_id + node_id)
      else:
        dag.add_node(node_id - label_anchor_id)

    # assign node color and label
    node_color = ''
    node_labels = {}
    for node_id in dag.nodes:
      if node_id > 0 and node_id < label_anchor_id:
        node_color += 'c'
        node_labels[node_id] = abs(node_id)
      elif node_id > 0 and node_id >= label_anchor_id:
        node_color += 'w'
        node_labels[node_id] = dag.node[node_id - label_anchor_id]['_label_']
      elif node_id < 0 and node_id > -1 * label_anchor_id:
        node_color += 'm'
        node_labels[node_id] = abs(node_id)
      else:
        node_color += 'w'
        node_labels[node_id] = dag.node[node_id + label_anchor_id]['_label_']
    pos = {}
    a_nodes = []
    e_nodes = []
    for node_id in dag.nodes:
      if node_id > 0 and node_id < label_anchor_id:
        a_nodes.append(node_id)
      elif node_id < 0 and node_id > -1 * label_anchor_id:
        e_nodes.append(node_id)

    a_nodes.sort(key=abs)
    e_nodes.sort(key=abs)
    a_node_y = 0
    e_node_y = 0.035
    a_offset = -0.5 if len(a_nodes) % 2 == 0 else 0
    e_offset = -0.5 if len(e_nodes) % 2 == 0 else 0
    a_node_x_min = -1 * len(a_nodes)/2 + a_offset
    e_node_x_min = -1 * len(e_nodes)/2 + e_offset
    a_node_x = a_node_x_min
    e_node_x = e_node_x_min
    node_step = 1
    for a_id in a_nodes:
      pos[a_id] = [a_node_x, a_node_y]
      pos[a_id + label_anchor_id] = [a_node_x, a_node_y - 0.01]
      a_node_x += node_step
    for e_id in e_nodes:
      pos[e_id] = [e_node_x, e_node_y]
      pos[e_id - label_anchor_id] = [e_node_x, e_node_y + 0.01]
      e_node_x += node_step

    nx.draw(dag, pos=pos,
            node_size=500, node_color=node_color,
            labels=node_labels, node_shape='o', font_size=8.3, label='abc')

    legend_x = max(a_node_x, e_node_x) - 0.85
    legend_y = 0.02
    a_bbox_props = dict(boxstyle='square,pad=0.3', fc='c', ec='b', lw=0)
    plt.text(legend_x - 0.0025, legend_y, '  Artifacts  ', bbox=a_bbox_props)
    e_bbox_props = dict(boxstyle='square,pad=0.3', fc='m', ec='b', lw=0)
    plt.text(legend_x - 0.0025, legend_y - 0.007, 'Executions',
             bbox=e_bbox_props)

    x_lim_left = min(a_node_x_min, e_node_x_min) - 0.5
    x_lim_right = min(1 - 0.05 * len(a_nodes), max(a_node_x, e_node_x))

    x_lim_left = max(-2 - 1.5/len(a_nodes),
                     min(a_node_x_min, e_node_x_min) - 1.0)
    x_lim_right = max(a_node_x, e_node_x) + 0.1
    plt.xlim(x_lim_left, x_lim_right)

    plt.show()


class ReadonlyMetadataStore(object):
  """An ml-metadata store that provides read-only methods for notebooks."""

  def __init__(self, store):
    """Initializes a ReadonlyMetadataStore with given store.

    Args:
      store: An instance of `metadata_store.MetadataStore`.
    """
    self.metadata_store = store
    self._lineage_graph_helper = _LineageGraphHelper(store)

  def get_df_from_single_artifact_or_execution(self, obj):
    """Returns a `pd.DataFrame` based on an artifact/execution properties.

    Args:
      obj: An instance of `metadata_store_pb2.Artifact` or
           `metadata_store_pb2.Execution`.

    Returns:
      A `pd.DataFrame` to display the properties of an artifact/execution.
    """
    data = {}
    if isinstance(obj, metadata_store_pb2.Artifact):
      data['URI'] = obj.uri
    for p in obj.properties:
      data[p.upper()] = _get_value_str(obj.properties[p])
    for p in obj.custom_properties:
      data[p.upper()] = _get_value_str(obj.custom_properties[p])
    return pd.DataFrame.from_dict(
        data=data, orient='index', columns=['']).fillna('-')

  def get_df_from_artifacts_or_executions(self, objects):
    """Returns a `pd.DataFrame` of given artifacts'/executions' properties."""
    data = {}
    for obj in objects:
      col_map = {}
      if isinstance(obj, metadata_store_pb2.Artifact):
        col_map['URI'] = obj.uri
      for p in obj.properties:
        col_map[p.upper()] = _get_value_str(obj.properties[p])
      for p in obj.custom_properties:
        col_map[p.upper()] = _get_value_str(obj.custom_properties[p])
      data[obj.id] = col_map
    df = pd.DataFrame.from_dict(data=data, orient='index').fillna('-')
    df.index.name = 'ID'
    return df

  def get_artifact_df(self, artifact_id):
    """Returns a `pd.DataFrame` for an artifact with `artifact_id`.

    Args:
      artifact_id: An `int` indicating the id of an artifact in the store.

    Returns:
      A `pd.DataFrame` to display the properties of the artifact corresponding
      to `artifact_id` or None if no such artifact exists in the store.
    """
    artifacts = self.metadata_store.get_artifacts_by_id([artifact_id])
    return (
        self.get_df_from_single_artifact_or_execution(artifacts[0])
        if artifacts else None
    )

  def get_execution_df(self, execution_id):
    """Returns a `pd.DataFrame` for an execution with `execution_id`.

    Args:
      execution_id: An `int` indicating the id of an execution in the store.

    Returns:
      A `pd.DataFrame` to display the properties of the execution corresponding
      to `execution_id` or None if no such execution exists in the store.
    """
    executions = self.metadata_store.get_executions_by_id([execution_id])
    return (
        self.get_df_from_single_artifact_or_execution(executions[0])
        if executions else None
    )

  def get_artifacts_of_type_df(self, type_name):
    """Returns a `pd.DataFrame` for all artifacts of given `type_name`.

    Args:
      type_name: A `str` indicating the name of an artifact type in the store.

    Returns:
      A `pd.DataFrame` to display the properties of all artifacts with given
      type in the store.
    """
    return self.get_df_from_artifacts_or_executions(
        self.metadata_store.get_artifacts_by_type(type_name))

  def get_executions_of_type_df(self, type_name):
    """Returns a `pd.DataFrame` for all executions of given `type_name`.

    Args:
      type_name: A `str` indicating the name of an execution type in the store.

    Returns:
      A `pd.DataFrame` to display the properties of all executions with given
      type in the store.
    """
    return self.get_df_from_artifacts_or_executions(
        self.metadata_store.get_executions_by_type(type_name))

  def get_source_artifact_of_type(self, artifact_id, source_type_name):
    """Returns the source artifact of `source_type_name` for `artifact_id`.

    This method recursively traverses the events and associated executions that
    led to generating `artifact_id` to find an artifact of type
    `source_type_name` that was an input for these events.

    Args:
      artifact_id: A `int` indicating the id of an artifact.
      source_type_name: A `str` indicating the type of an artifact that is
          a direct or indirect input for generating `artifact_id`.

    Returns:
      A `metadata_store_pb2.Artifact` of type `source_type_name` that is a
      direct/indirect input for generating `artifact_id` or `None` if no such
      artifact exists.
    """
    a_events = self.metadata_store.get_events_by_artifact_ids([artifact_id])
    for a_event in a_events:
      if _is_input_event(a_event):
        continue
      [execution] = self.metadata_store.get_executions_by_id(
          [a_event.execution_id])
      e_events = self.metadata_store.get_events_by_execution_ids([execution.id])
      for e_event in e_events:
        if _is_output_event(e_event):
          continue
        [artifact] = self.metadata_store.get_artifacts_by_id(
            [e_event.artifact_id])
        [artifact_type] = self.metadata_store.get_artifact_types_by_id(
            [artifact.type_id])
        if artifact_type.name == source_type_name:
          return artifact
        input_artifact = self.get_source_artifact_of_type(
            artifact.id, source_type_name)
        if input_artifact:
          return input_artifact

  def get_dest_artifact_of_type(self, artifact_id, dest_type_name):
    """Returns the destination artifact of `dest_type_name` from `artifact_id`.

    This method recursively traverses the events and associated executions that
    consumed `artifact_id` to find an artifact of type `dest_type_name` that was
    an output for these events.

    Args:
      artifact_id: A `int` indicating the id of an artifact.
      dest_type_name: A `str` indicating the type of an artifact that is
          a output of an event that directly/indirectly consumed `artifact_id`.

    Returns:
      A `metadata_store_pb2.Artifact` of type `dest_type_name` that is a
      direct/indirect output from `artifact_id` or `None` if no such artifact
      exists.
    """
    a_events = self.metadata_store.get_events_by_artifact_ids([artifact_id])
    for a_event in a_events:
      if _is_output_event(a_event):
        continue
      [execution] = self.metadata_store.get_executions_by_id(
          [a_event.execution_id])
      e_events = self.metadata_store.get_events_by_execution_ids(
          [execution.id])
      for e_event in e_events:
        if _is_input_event(e_event):
          continue
        [artifact] = self.metadata_store.get_artifacts_by_id(
            [e_event.artifact_id])
        [artifact_type] = self.metadata_store.get_artifact_types_by_id(
            [artifact.type_id])
        if artifact_type.name == dest_type_name:
          return artifact
        dest_artifact = self.get_dest_artifact_of_type(
            artifact.id, dest_type_name)
        if dest_artifact:
          return dest_artifact

  def get_execution_for_output_artifact(self, artifact_id, type_name):
    """Returns the execution of `type_name` that generated `artifact_id`.

    Args:
      artifact_id: A `int` indicating the id of an artifact.
      type_name: A `str` indicating the type of an Execution that generated
        `artifact_id`.

    Returns:
      A `metadata_store_pb2.Execution` of type `type_name` that generated
      `artifact_id` or `None` if no such execution exists.
    """
    a_events = self.metadata_store.get_events_by_artifact_ids([artifact_id])
    for a_event in a_events:
      if _is_input_event(a_event):
        continue
      [execution] = self.metadata_store.get_executions_by_id(
          [a_event.execution_id])
      [execution_type] = self.metadata_store.get_execution_types_by_id(
          [execution.type_id])
      if execution_type.name == type_name:
        return execution

  def display_artifact_and_execution_properties(self, artifact_id,
                                                execution_type_name):
    """Displays properties of artifact and the execution that generated it.

    Args:
      artifact_id: A `int` indicating the id of an artifact.
      execution_type_name: A `str` indicating the type of an execution that
          generated `artifact_id`.
    """
    execution = self.get_execution_for_output_artifact(
        artifact_id, execution_type_name)
    if not execution:
      return
    execution_id = execution.id

    # Get data frames to visualize the artifact and execution properties.
    artifact_df, execution_df = (
        self.get_artifact_df(artifact_id), self.get_execution_df(execution_id)
    )

    # Style the data frames to set captions.
    artifact_df_styler = artifact_df.style.set_caption(
        'Properties for Artifact {}'.format(artifact_id))
    execution_df_styler = execution_df.style.set_caption(
        'Properties for Execution {} that generated Artifact {}'.format(
            execution_id, artifact_id))

    # Display the HTML.
    # pylint: disable=protected-access
    display_html(
        artifact_df_styler._repr_html_() + execution_df_styler._repr_html_(),
        raw=True)
    # pylint: enable=protected-access

  def compare_artifact_pair_and_execution_properties(
      self, artifact_id, other_artifact_id, execution_type_name):
    """Displays properties of 2 artifacts and executions that generated them.

    Args:
      artifact_id: A `int` indicating the id of one artifact.
      other_artifact_id: A `int` indicating the id of another artifact.
      execution_type_name: A `str` indicating the type of executions that
          generated `artifact_id` and `other_artifact_id`.
    """
    # Get data frame to visualize properties of the 2 artifacts.
    df = self.get_df_from_artifacts_or_executions(
        self.metadata_store.get_artifacts_by_id(
            [artifact_id, other_artifact_id]))
    artifacts_df_styler = df.style.set_caption(
        'Properties for Artifacts {}, {}'.format(
            artifact_id, other_artifact_id))

    # Compare properties of the executions that generated these artifacts.
    execution = self.get_execution_for_output_artifact(
        artifact_id, execution_type_name)
    other_execution = self.get_execution_for_output_artifact(
        other_artifact_id, execution_type_name)
    if not execution or not other_execution:
      return
    executions_df = self.get_df_from_artifacts_or_executions([
        execution, other_execution])
    executions_df_styler = executions_df.style.set_caption(
        'Properties for Executions that generated Artifacts {}, {}'.format(
            artifact_id, other_artifact_id))

    # Display the HTML.
    # pylint: disable=protected-access
    display_html(
        artifacts_df_styler._repr_html_() + executions_df_styler._repr_html_(),
        raw=True)
    # pylint: enable=protected-access

  def plot_artifact_lineage(self, artifact_id, max_depth=None):
    """Computes and plots the lineage graph for `artifact_id` upto `max_depth`.

    Args:
      artifact_id: An `int` indicating the id of an Artifact.
      max_depth: (Optional): An `int` indicating how far back the lineage
          should be computed for `artifact_id`. By default the entire lineage
          is computed.
    """
    self._lineage_graph_helper.plot_artifact_lineage(
        self._lineage_graph_helper.get_artifact_lineage(
            artifact_id, max_depth=max_depth))
