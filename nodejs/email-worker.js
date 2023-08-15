const { ZBClient } = require('zeebe-node')

const zbc = new ZBClient({
	camundaCloud: {
		clusterId: 'YOUR_CLUSTER_ID',
		clientId: 'YOUR_CLIENT_ID',
		clientSecret: 'YOUR_CLIENT_SECRET',
	},
})

const worker = zbc.createWorker({
	taskType: 'email',
	taskHandler: (job) => {
		const { message_content } = job.variables
		worker.log(`Sending email with message content: ${message_content}`)
		job.complete()
	}
})
