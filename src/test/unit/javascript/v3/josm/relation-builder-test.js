/* global Java */


import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as util from 'josm/util'
import {WayBuilder, NodeBuilder, RelationBuilder} from 'josm/builder'

const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const Relation = Java.type('org.openstreetmap.josm.data.osm.Relation')
const Node = Java.type('org.openstreetmap.josm.data.osm.Node')
const ArrayList = Java.type('java.util.ArrayList')

const suites = []

suites.push(suite("builder method 'member'",
  test('member - with a node, a way or a relation ', function () {
    const node = NodeBuilder.create()
    let rm = RelationBuilder.member(node)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === node, 'unexpected member (node)')

    const way = WayBuilder.create()
    rm = RelationBuilder.member(way)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === way, 'unexpected member (way)')

    const relation = RelationBuilder.create()
    rm = RelationBuilder.member(relation)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === relation, 'unexpected member (relation)')
  }),

  test('member - with a node - null or undefined not supported ', function () {
    expectAssertionError(function () {
      RelationBuilder.member(null)
    })

    expectAssertionError(function () {
      RelationBuilder.member(undefined)
    })
  }),

  test('member - with a node - unexpected value ', function () {
    expectAssertionError(function () {
      RelationBuilder.member('not supported')
    })
  }),

  test('member - with a role and a node', function () {
    const node = NodeBuilder.create()
    const rm = RelationBuilder.member('myrole', node)
    util.assert(rm.getRole() === 'myrole',
      'unexpected role, got {0}', rm.getRole())
    util.assert(rm.getMember() === node, 'unexpected member')
  }),

  test('member - with a role and a node - role can be null or undefined', function () {
    const node = NodeBuilder.create()
    let rm = RelationBuilder.member(null, node)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === node, 'unexpected member')

    rm = RelationBuilder.member(undefined, node)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === node, 'unexpected member')
  }),

  test('member - no arguments', function () {
    expectAssertionError(function () {
      RelationBuilder.member()
    })
  }),

  test('member - more than two arguments', function () {
    expectAssertionError(function () {
      RelationBuilder.member('role', NodeBuilder.create(), 'a string')
    })
  })
))

suites.push(suite('simple local, global and proxy relation ',
  test('local relation - most simple relation', function () {
    const relation = RelationBuilder.create()
    util.assert(relation instanceof Relation, 'expected a relation')
    util.assert(util.isSomething(relation), 'expected a relation object')
    util.assert(relation.getUniqueId() < 0, 'id should be negative')
  }),

  test('global relation - most simple relation', function () {
    const relation = RelationBuilder.create(12345)
    util.assert(util.isSomething(relation), 'expected a relation object')
    util.assert(relation.getUniqueId() === 12345, 'id should be 12345')
  }),

  test('proxy relation', function () {
    const relation = RelationBuilder.createProxy(12345)
    util.assert(util.isSomething(relation), 'expected a relation object')
    util.assert(relation.getUniqueId() === 12345, 'id should be 12345')
    util.assert(relation.isIncomplete, 'should be incomplete')
  })
))

suites.push(suite('using withId(...) to create a relation',
  test('withId(id)', function () {
    const relation = RelationBuilder.withId(12345).create()
    util.assert(util.isSomething(relation), 'expected a relation object')
    util.assert(relation.getUniqueId() === 12345, 'id should be 12345')
  }),

  test('withId(id,version)', function () {
    const relation = RelationBuilder.withId(12345, 9).create()
    util.assert(util.isSomething(relation), 'expected a relation object')
    util.assert(relation.getUniqueId() === 12345, 'id should be 12345')
    util.assert(relation.getVersion() === 9, 'version should be 9')
  }),

  test('withId(id) - illegal id', function () {
    expectAssertionError('id must not be null', function () {
      RelationBuilder.withId(null).create()
    })
    expectAssertionError('id must not be undefined', function () {
      RelationBuilder.withId(undefined).create()
    })
    expectAssertionError('id must not be 0', function () {
      RelationBuilder.withId(0).create()
    })
    expectAssertionError('id must not be negative', function () {
      RelationBuilder.withId(-1).create()
    })
    expectAssertionError('id must not be a string', function () {
      RelationBuilder.withId('1').create()
    })
  }),

  test('withId(id,version) - illegal version', function () {
    expectAssertionError('version must not be null', function () {
      RelationBuilder.withId(12345, null).create()
    })
    expectAssertionError('version must not be undefined', function () {
      RelationBuilder.withId(12345, undefined).create()
    })
    expectAssertionError('version must not be 0', function () {
      RelationBuilder.withId(12345, 0).create()
    })
    expectAssertionError('version must not be negative', function () {
      RelationBuilder.withId(12345, -1).create()
    })
    expectAssertionError('version must not be a string', function () {
      RelationBuilder.withId(12345, '1').create()
    })
  })
))

suites.push(suite('using withTags()',
  test('tage type=route', function () {
    const r = RelationBuilder.withTags({ type: 'route' }).create()
    util.assert(r.getUniqueId() < 0, 'unexpected id')
    util.assert(r.get('type') === 'route', 'unexpected tag value')
  }),

  test('empty tags should be ok', function () {
    RelationBuilder.withTags({}).create()
  }),

  test('null tags should be ok', function () {
    RelationBuilder.withTags(null).create()
  }),

  test('undefined tags should be OK', function () {
    RelationBuilder.withTags(undefined).create()
  }),

  test('tag key is normalized, tag value is not not normalized', function () {
    const r = RelationBuilder.withTags({ ' type ': ' route ' }).create()
    util.assert(r.hasKey('type'), "should have normalized tag key 'type'")
    util.assert(r.get('type') === ' route ',
      'tag value should not be normalized, value is <{0}>',
      r.get('type'))
  }),

  test('null tags are ignored', function () {
    const r = RelationBuilder.withTags({ type: null }).create()
    util.assert(!r.hasTag('type'), 'should not have a null tag')
  }),

  test('undefined tags are ignored', function () {
    const r = RelationBuilder.withTags({ type: undefined }).create()
    util.assert(!r.hasTag('type'), 'should not have an undefined tag')
  }),

  test('tag values are converted to a string', function () {
    const r = RelationBuilder.withTags({ type: 2 }).create()
    util.assert(r.get('type') === '2',
      'tag should be converted to a string, value is <{0}>, type is {1}',
      r.get('type'), typeof r.get('type'))
  })
))

suites.push(suite('using withMembers ',
  test('one member - explicit member object', function () {
    const node = NodeBuilder.create()
    const r = RelationBuilder.withMembers(RelationBuilder.member('myrole', node)).create()
    util.assert(r.getMembersCount() === 1, 'unexpected number of members')
    const rm = r.getMember(0)
    util.assert(rm.getRole() === 'myrole', 'unexpected role')
    util.assert(rm.getMember() === node, 'unexpected member node')
  }),

  test('one member - passing in a naked node', function () {
    const node = NodeBuilder.create()
    const r = RelationBuilder.withMembers(node).create()
    util.assert(r.getMembersCount() === 1, 'unexpected number of members')
    const rm = r.getMember(0)
    util.assert(rm.getRole() === '', 'unexpected role')
    util.assert(rm.getMember() === node, 'unexpected member node')
  }),

  test('three members - as constargs', function () {
    const n = NodeBuilder.create()
    const w = WayBuilder.create()
    const r = RelationBuilder.create()
    const relation = RelationBuilder.withMembers(n, RelationBuilder.member('role.1', w), r).create()
    util.assert(relation.getMembersCount() === 3,
      'unexpected number of members')
    let rm = relation.getMember(0)
    util.assert(rm.getRole() === '', 'unexpected role for node')
    util.assert(rm.getMember() === n, 'unexpected member node')

    rm = relation.getMember(1)
    util.assert(rm.getRole() === 'role.1', 'unexpected role for way')
    util.assert(rm.getMember() === w, 'unexpected member way')

    rm = relation.getMember(2)
    util.assert(rm.getRole() === '', 'unexpected role for relation')
    util.assert(rm.getMember() === r, 'unexpected member relation')
  }),

  test('three members - as array', function () {
    const n = NodeBuilder.create()
    const w = WayBuilder.create()
    const r = RelationBuilder.create()
    const relation = RelationBuilder.withMembers([n, RelationBuilder.member('role.1', w), r]).create()
    util.assert(relation.getMembersCount() === 3,
      'unexpected number of members')
    let rm = relation.getMember(0)
    util.assert(rm.getRole() === '', 'unexpected role for node')
    util.assert(rm.getMember() === n, 'unexpected member node')

    rm = relation.getMember(1)
    util.assert(rm.getRole() === 'role.1', 'unexpected role for way')
    util.assert(rm.getMember() === w, 'unexpected member way')

    rm = relation.getMember(2)
    util.assert(rm.getRole() === '', 'unexpected role for relation')
    util.assert(rm.getMember() === r, 'unexpected member relation')
  }),

  test('three members - as list', function () {
    const n = NodeBuilder.create()
    const w = WayBuilder.create()
    const r = RelationBuilder.create()
    const list = new ArrayList()
    list.add(n)
    list.add(RelationBuilder.member('role.1', w))
    list.add(r)
    const relation = RelationBuilder.withMembers(list).create()
    util.assert(relation.getMembersCount() === 3,
      'unexpected number of members')
    let rm = relation.getMember(0)
    util.assert(rm.getRole() === '', 'unexpected role for node')
    util.assert(rm.getMember() === n, 'unexpected member node')

    rm = relation.getMember(1)
    util.assert(rm.getRole() === 'role.1', 'unexpected role for way')
    util.assert(rm.getMember() === w, 'unexpected member way')

    rm = relation.getMember(2)
    util.assert(rm.getRole() === '', 'unexpected role for relation')
    util.assert(rm.getMember() === r, 'unexpected member relation')
  }),

  test('null and undefined members should be ignored', function () {
    let r = RelationBuilder.withMembers(null).create()
    util.assert(r.getMembersCount() === 0,
      '1 - unexpected number of members')

    r = RelationBuilder.withMembers(undefined).create()
    util.assert(r.getMembersCount() === 0,
      '2 - unexpected number of members')

    r = RelationBuilder.withMembers([null, undefined]).create()
    util.assert(r.getMembersCount() === 0,
      '3 - unexpected number of members')
  })
))

suites.push(suite('using create(...) with optional arguments',
  test('setting {version: ..}', function () {
    const r = RelationBuilder.create(12345, { version: 9 })
    util.assert(r instanceof Relation, 'expected a relation')
    util.assert(r.getVersion() === 9,
      'expected version 9, got {0}', r.getVersion())

    expectAssertionError('version must not be null', function () {
      RelationBuilder.create(12345, { version: null })
    })

    expectAssertionError('version must not be undefined', function () {
      RelationBuilder.create(12345, { version: undefined })
    })

    expectAssertionError('version must not be 0', function () {
      RelationBuilder.create(12345, { version: 0 })
    })

    expectAssertionError('version must not be negative', function () {
      RelationBuilder.create(12345, { version: -1 })
    })

    expectAssertionError('version must not be a string', function () {
      RelationBuilder.create(12345, { version: '1' })
    })
  }),

  test('setting {tags: ..}', function () {
    let r = RelationBuilder.create(12345, { tags: { type: 'route' } })
    util.assert(r instanceof Relation, 'expected a relation')
    util.assert(r.get('type') === 'route', 'Tag not set as expected')

    // null and undefined are OK
    r = RelationBuilder.create(12345, { tags: null })
    r = RelationBuilder.create(12345, { tags: undefined })

    r = RelationBuilder.create(12345, { tags: { ' type ': ' route ' } })
    util.assert(r.hasKey('type'), "should have normalized key 'type'")
    util.assert(r.get('type') === ' route ',
      'value should not be normalized, got {0}', r.get('type'))
  }),

  test('setting {members: ..}', function () {
    let r = RelationBuilder.create(12345, {
      members: [RelationBuilder.member('role.1', NodeBuilder.create())]
    })
    util.assert(r instanceof Relation, 'expected a relation')
    util.assert(r.getMembersCount() === 1, '1- unexpected number of members')
    util.assert(r.getMember(0).getRole() === 'role.1', 'unexpected role')
    util.assert(r.getMember(0).getMember() instanceof Node,
      'unexpected type of member object')

    // null and undefined are OK
    r = RelationBuilder.create(12345, { members: null })
    util.assert(r.getMembersCount() === 0,
      '2 - unexpected number of members')
    r = RelationBuilder.create(12345, { members: undefined })
    util.assert(r.getMembersCount() === 0,
      '3 - unexpected number of members')
  }),

  test('optional arguments may be null or undefined', function () {
    let r = RelationBuilder.create(12345, null)
    util.assert(r instanceof Relation, '1 - expected a relation')

    r = RelationBuilder.create(12345, undefined)
    util.assert(r instanceof Relation, '2 - expected a relation')
  })
))

suites.push(suite('forDataSet test cases',
  test('instance context - create with defined dataset', function () {
    const ds = new DataSet()
    const n = RelationBuilder.forDataSet(ds).create()
    util.assert(n.getDataSet() === ds,
      '1 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  }),
  test('static context - create with defined dataset', function () {
    const ds = new DataSet()
    const n = RelationBuilder.forDataSet(ds).create()
    util.assert(n.getDataSet() === ds,
      '2 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  })
))


export function run() {
  return suites
    .map(function (suite) { return suite.run() })
    .reduce(function (a, b) { return a + b })
}