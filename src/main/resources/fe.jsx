import React, { useState, useRef, useCallback, useEffect } from 'react';
import {
  ReactFlow,
  ReactFlowProvider,
  addEdge,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
} from 'https://esm.sh/@xyflow/react';

import 'https://esm.sh/@xyflow/react/dist/style.css';

// --- Custom Node for LLM Prompts ---
const LLMNode = ({ data }) => (
    <div style={{
      border: '2px solid #5a67d8', borderRadius: '8px', padding: '15px',
      backgroundColor: '#ebf8ff', width: 250,
      boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)'
    }}>
      <div style={{ fontWeight: 'bold', marginBottom: '10px', color: '#2c5282' }}>📝 LLM Node</div>
      <label style={{ display: 'block', fontSize: '12px', color: '#4a5568' }}>Label:</label>
      <input defaultValue={data.label} style={{ width: '100%', padding: '4px', border: '1px solid #cbd5e0', borderRadius: '4px', boxSizing: 'border-box', marginBottom: '10px' }} className="nodrag" />
      <label style={{ display: 'block', fontSize: '12px', color: '#4a5568' }}>Prompt Template:</label>
      <textarea defaultValue={data.promptTemplate} rows={4} style={{ width: '100%', padding: '4px', border: '1px solid #cbd5e0', borderRadius: '4px', boxSizing: 'border-box' }} className="nodrag" />
    </div>
);

const nodeTypes = { llmNode: LLMNode };
const initialNodes = [{ id: '1', type: 'input', data: { label: 'Start' }, position: { x: 50, y: 50 } }];
let id = 2;
const getId = () => `${id++}`;

// --- Main App Component ---
const App = () => {
  const reactFlowWrapper = useRef(null);
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [reactFlowInstance, setReactFlowInstance] = useState(null);

  // State for workflow management
  const [workflows, setWorkflows] = useState([]);
  const [selectedWorkflowId, setSelectedWorkflowId] = useState('');
  const [workflowName, setWorkflowName] = useState('My New Workflow');

  // State for execution
  const [initialVariables, setInitialVariables] = useState('{\n  "topic": "the moon",\n  "style": "Dr. Seuss"\n}');
  const [executionResult, setExecutionResult] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [notification, setNotification] = useState({ message: '', type: '' });

  // Fetch workflows on mount
  useEffect(() => {
    const fetchWorkflows = async () => {
        try {
            const response = await fetch('/api/workflows');
            if (!response.ok) throw new Error('Failed to fetch workflows');
            const data = await response.json();
            setWorkflows(data);
            if (data.length > 0) {
                loadWorkflow(data[0].id, data);
            }
        } catch (error) {
            showNotification(error.message, 'error');
        }
    };
    fetchWorkflows();
  }, []);

  const onConnect = useCallback((params) => setEdges((eds) => addEdge(params, eds)), [setEdges]);

  const onDragOver = useCallback((event) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, []);

  const onDrop = useCallback((event) => {
    event.preventDefault();
    const type = event.dataTransfer.getData('application/reactflow');
    if (typeof type === 'undefined' || !type) return;

    const position = reactFlowInstance.screenToFlowPosition({ x: event.clientX, y: event.clientY });
    const newNode = { id: getId(), type, position, data: { label: `${type} node` } };
    if (type === 'llmNode') {
      newNode.data = { label: 'New LLM Node', promptTemplate: 'Your prompt here...' };
    }
    setNodes((nds) => nds.concat(newNode));
  }, [reactFlowInstance, setNodes]);

  const onDragStart = (event, nodeType) => {
    event.dataTransfer.setData('application/reactflow', nodeType);
    event.dataTransfer.effectAllowed = 'move';
  };

  const showNotification = (message, type) => {
    setNotification({ message, type });
    setTimeout(() => setNotification({ message: '', type: '' }), 4000);
  };

  const loadWorkflow = (id, wfList) => {
      const workflowToLoad = (wfList || workflows).find(wf => wf.id.toString() === id.toString());
      if (workflowToLoad) {
          setSelectedWorkflowId(workflowToLoad.id);
          setWorkflowName(workflowToLoad.name);
          setNodes(workflowToLoad.nodesJson ? JSON.parse(workflowToLoad.nodesJson) : []);
          setEdges(workflowToLoad.edgesJson ? JSON.parse(workflowToLoad.edgesJson) : []);
      }
  };

  const onSave = async () => {
    if (!reactFlowInstance) return;

    const nodesJson = JSON.stringify(nodes.map(n => ({ id: n.id, type: n.type, position: n.position, data: n.data })));
    const edgesJson = JSON.stringify(edges);
    const workflowData = { name: workflowName, nodesJson, edgesJson };

    const url = selectedWorkflowId ? `/api/workflows/${selectedWorkflowId}` : '/api/workflows';
    const method = selectedWorkflowId ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(workflowData) });
        if (!response.ok) throw new Error(`Failed to save: ${await response.text()}`);
        const saved = await response.json();
        showNotification(`Workflow "${saved.name}" saved!`, 'success');
        // Refresh workflow list
        const wfResponse = await fetch('/api/workflows');
        const data = await wfResponse.json();
        setWorkflows(data);
        setSelectedWorkflowId(saved.id);
    } catch (error) {
        showNotification(error.message, 'error');
    }
  };

  const onExecuteFull = async () => {
      if (!selectedWorkflowId) {
          showNotification('Please save or load a workflow first.', 'error');
          return;
      }
      setIsLoading(true);
      setExecutionResult('');
      try {
          const parsedVars = JSON.parse(initialVariables);
          const requestBody = { modelId: 'openai', initialVariables: parsedVars }; // Hardcoding openai for now

          const response = await fetch(`/api/workflows/${selectedWorkflowId}/execute-full`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(requestBody)
          });

          if (!response.ok) throw new Error(`Execution failed: ${await response.text()}`);
          const data = await response.json();
          setExecutionResult(data.result);
      } catch (error) {
          showNotification(error.message, 'error');
      } finally {
          setIsLoading(false);
      }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif', color: '#2d3748' }}>
      <ReactFlowProvider>
        <div style={{ flex: 1, height: '100%' }} ref={reactFlowWrapper}>
          <ReactFlow nodes={nodes} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} onConnect={onConnect} onInit={setReactFlowInstance} onDrop={onDrop} onDragOver={onDragOver} nodeTypes={nodeTypes} fitView>
            <Controls />
            <Background />
          </ReactFlow>
        </div>
        <aside style={{ width: '350px', borderLeft: '1px solid #e2e8f0', padding: '15px', backgroundColor: '#f7fafc', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <h2 style={{ margin: 0 }}>AI Workflow Orchestrator</h2>

          <div>
            <label>Load Workflow:</label>
            <select value={selectedWorkflowId} onChange={(e) => loadWorkflow(e.target.value)} style={{ width: '100%', padding: '8px', marginTop: '5px' }}>
                <option value="">Select a workflow</option>
                {workflows.map(wf => <option key={wf.id} value={wf.id}>{wf.name}</option>)}
            </select>
          </div>

          <div>
            <label>Workflow Name:</label>
            <input type="text" value={workflowName} onChange={(e) => setWorkflowName(e.target.value)} style={{ width: '100%', padding: '8px', boxSizing: 'border-box', marginTop: '5px' }} />
          </div>

          <div>
            <div style={{ marginBottom: '10px' }}>Drag nodes to the canvas:</div>
            <div style={{ padding: '10px', border: '2px dashed #cbd5e0', borderRadius: '8px', marginBottom: '10px', cursor: 'grab', textAlign: 'center', backgroundColor: '#fff' }} onDragStart={(e) => onDragStart(e, 'llmNode')} draggable>LLM Node</div>
          </div>

          <button onClick={onSave} style={{ padding: '12px', backgroundColor: '#4299e1', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1em', cursor: 'pointer' }}>Save Workflow</button>

          <hr/>

          <div>
            <label>Initial Variables (JSON):</label>
            <textarea value={initialVariables} onChange={(e) => setInitialVariables(e.target.value)} rows={5} style={{ width: '100%', padding: '8px', boxSizing: 'border-box', marginTop: '5px', fontFamily: 'monospace' }} />
          </div>

          <button onClick={onExecuteFull} disabled={isLoading} style={{ padding: '12px', backgroundColor: isLoading ? '#a0aec0' : '#48bb78', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1em', cursor: 'pointer' }}>
            {isLoading ? 'Executing...' : 'Execute Full Workflow'}
          </button>

          <div>
            <label>Execution Result:</label>
            <pre style={{ backgroundColor: '#edf2f7', padding: '10px', borderRadius: '8px', minHeight: '100px', whiteSpace: 'pre-wrap', wordWrap: 'break-word', marginTop: '5px' }}>
                {executionResult}
            </pre>
          </div>

          {notification.message && <div style={{ position: 'absolute', bottom: '20px', right: '20px', padding: '15px', borderRadius: '8px', color: 'white', backgroundColor: notification.type === 'success' ? '#48bb78' : '#f56565', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>{notification.message}</div>}
        </aside>
      </ReactFlowProvider>
    </div>
  );
};

export default App;
