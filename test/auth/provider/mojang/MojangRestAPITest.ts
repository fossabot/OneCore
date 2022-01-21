/* eslint-disable @typescript-eslint/no-explicit-any */
import { MojangRestAPI, Session } from '../../../../lib/mojang/rest/MojangRestAPI'
import { expect } from 'chai'
import nock from 'nock'
import { MojangErrorCode, MojangResponse } from '../../../../lib/mojang/rest/MojangResponse'
import { assertResponse, expectFailure, expectSuccess } from '../../../common/RestResponseUtil'

function expectMojangResponse(res: MojangResponse<unknown>, responseCode: MojangErrorCode, negate = false): void {
    assertResponse(res)
    expect(res).to.have.property('mojangErrorCode')
    if(!negate) {
        expect(res.mojangErrorCode).to.equal(responseCode)
    } else {
        expect(res.mojangErrorCode).to.not.equal(responseCode)
    }
}

describe('[Mojang Rest API] Errors', () => {

    after(() => {
        nock.cleanAll()
    })

    it('Status (Offline)', async () => {

        const defStatusHack = MojangRestAPI['statuses']

        nock(MojangRestAPI.STATUS_ENDPOINT)
            .get('/check')
            .reply(500, 'Service temprarily offline.')

        const res = await MojangRestAPI.status()
        expectFailure(res)
        expect(res.data).to.be.an('array')
        expect(res.data).to.deep.equal(defStatusHack)

    }).timeout(2500)

    it('Authenticate (Invalid Credentials)', async () => {

        nock(MojangRestAPI.AUTH_ENDPOINT)
            .post('/authenticate')
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            .reply(403, (uri, requestBody: unknown): { error: string, errorMessage: string } => {
                return {
                    error: 'ForbiddenOperationException',
                    errorMessage: 'Invalid credentials. Invalid username or password.'
                }
            })

        const res = await MojangRestAPI.authenticate('user', 'pass', 'xxx', true)
        expectMojangResponse(res, MojangErrorCode.ERROR_INVALID_CREDENTIALS)
        expect(res.data).to.be.a('null')
        expect(res.error).to.not.be.a('null')

    })
})

describe('[Mojang Rest API] Status', () => {

    it('Status (Online)', async () => {

        const defStatusHack = MojangRestAPI['statuses']

        nock(MojangRestAPI.STATUS_ENDPOINT)
            .get('/check')
            .reply(200, defStatusHack)

        const res = await MojangRestAPI.status()
        expectSuccess(res)
        expect(res.data).to.be.an('array')
        expect(res.data).to.deep.equal(defStatusHack)

    }).timeout(2500)

})

describe('[Mojang Rest API] Auth', () => {
    
    it('Authenticate', async () => {

        nock(MojangRestAPI.AUTH_ENDPOINT)
            .post('/authenticate')
            .reply(200, (uri, requestBody: any): Session => {
                const mockResponse: Session = {
                    accessToken: 'abc',
                    clientToken: requestBody.clientToken,
                    selectedProfile: {
                        id: 'def',
                        name: 'username'
                    }
                }

                if(requestBody.requestUser) {
                    mockResponse.user = {
                        id: 'def',
                        properties: []
                    }
                }

                return mockResponse
            })

        const res = await MojangRestAPI.authenticate('user', 'pass', 'xxx', true)
        expectSuccess(res)
        expect(res.data!.clientToken).to.equal('xxx')
        expect(res.data).to.have.property('user')

    })

    it('Validate', async () => {

        nock(MojangRestAPI.AUTH_ENDPOINT)
            .post('/validate')
            .times(2)
            .reply((uri, requestBody: any) => {
                return [
                    requestBody.accessToken === 'abc' ? 204 : 403
                ]
            })

        const res = await MojangRestAPI.validate('abc', 'def')

        expectSuccess(res)
        expect(res.data).to.be.a('boolean')
        expect(res.data).to.equal(true)

        const res2 = await MojangRestAPI.validate('def', 'def')

        expectSuccess(res2)
        expect(res2.data).to.be.a('boolean')
        expect(res2.data).to.equal(false)

    })

    it('Invalidate', async () => {

        nock(MojangRestAPI.AUTH_ENDPOINT)
            .post('/invalidate')
            .reply(204)

        const res = await MojangRestAPI.invalidate('adc', 'def')

        expectSuccess(res)

    })

    it('Refresh', async () => {

        nock(MojangRestAPI.AUTH_ENDPOINT)
            .post('/refresh')
            .reply(200, (uri, requestBody: any): Session => {
                const mockResponse: Session = {
                    accessToken: 'abc',
                    clientToken: requestBody.clientToken,
                    selectedProfile: {
                        id: 'def',
                        name: 'username'
                    }
                }

                if(requestBody.requestUser) {
                    mockResponse.user = {
                        id: 'def',
                        properties: []
                    }
                }

                return mockResponse
            })

        const res = await MojangRestAPI.refresh('gfd', 'xxx', true)
        expectSuccess(res)
        expect(res.data!.clientToken).to.equal('xxx')
        expect(res.data).to.have.property('user')

    })

})