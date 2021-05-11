const { ZBClient } = require('zeebe-node')

const zbc = new ZBClient({
	camundaCloud: {
		clusterId: '365eed98-16c1-4096-bb57-eb8828ed131e',
		clientId: 'GZVO3ALYy~qCcD3MYq~sf0GIszNzLE_z',
		clientSecret: '.RPbZc6q0d6uzRbB4LW.B8lCpsxbBEpmBX0AHQGzINf3.KK9RkzZW1aDaZ-7WYNJ',
	},
})

zbc.createWorker({
	taskType: 'email',
	taskHandler: (job, _, worker) => {
		const { message_content } = job.variables
		worker.log(`Sending email with message content: ${message_content}`)
		job.complete()
	}
})
