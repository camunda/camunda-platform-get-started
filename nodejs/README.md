# Camunda Platform 8 - Get Started - Node.js

This guide explains how to setup a node.js project to automate a process using
[Camunda Platform 8](https://camunda.com/products/cloud/).

# Install dependencies

The open source library [zeebe-node](https://www.npmjs.com/package/zeebe-node)
provides a Zeebe client.

```
npm install --save zeebe-node
```

# Create Client

If we want to connect to a Camunda Platform 8 SaaS cluster we need the `clusterId`
from the [Clusters details
page](https://docs.camunda.io/docs/components/console/manage-clusters/create-cluster/),
a `clientId` and `clientSecret` from a [client credentials
pair](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/). 

```javascript
const { ZBClient } = require('zeebe-node')

const zbc = new ZBClient({
	camundaCloud: {
		clusterId: '365eed98-16c1-4096-bb57-eb8828ed131e',
		clientId: 'GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z',
		clientSecret: '.RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ',
	},
})
```

If you are using a self managed Camunda Platform 8 cluster, you create the client
without parameters.

```javascript
const { ZBClient } = require('zeebe-node')

const zbc = new ZBClient()
```


# Deploy Process and Start Instance

To deploy a process you have to specify the filepath of the BPMN file.

```
await zbc.deployProcess(['../process/send-email.bpmn'])
```

To start a new instance you have to specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```
const result = await zbc.createProcessInstance('send-email', {
	message_content: 'Hello from the node.js get started',
})
console.log(result)
```

For the complete code see the
[`deploy-and-start-instance.js`](deploy-and-start-instance.js) file. You can
run it using the following command.

```bash
node deploy-and-start-instance.js
```

# Job Worker

To complete a [service
task](https://docs.camunda.io/docs/reference/bpmn-processes/service-tasks/service-tasks/),
a [job
worker](https://docs.camunda.io/docs/product-manuals/concepts/job-workers) has
to be subscribed the to task type defined in the process, i.e. `email`.

```
zbc.createWorker({
	taskType: 'email',
	taskHandler: (job, _, worker) => {
		const { message_content } = job.variables
		worker.log(`Sending email with message content: ${message_content}`)
		job.complete()
	}
})
```

For the complete code see the [`email-worker.js`](email-worker.js) file. You can
run it using the following command.

```bash
node email-worker.js
```

To make an job available, a user task has to be completed, follow the
instructions in [the guide](../README.md#complete-the-user-task).
