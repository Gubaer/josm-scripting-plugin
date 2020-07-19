'use strict'

const URL_PREFIXES = [
  { prefix: 'org.openstreetmap.josm', url: 'http://josm.openstreetmap.de/doc/' },
  { prefix: 'java.', url: 'http://docs.oracle.com/javase/6/docs/api/' },
  { prefix: 'javax.swing.', url: 'http://docs.oracle.com/javase/6/docs/api/ ' }
]

const util = require('util')

function safeHtmlFilename (fn) {
  return fn.replace(/[^a-zA-Z0-9$\-_.]/g, '_') + '.html'
}

function matchingUrlPrefixForType (type) {
  const entry = URL_PREFIXES.find(function (urlPrefix) {
    return type.indexOf(urlPrefix.prefix) === 0
  })
  return entry ? entry.url : undefined
}

function resolveClassReferences (str) {
  const self = this
  if (str == null || str === undefined) return ''
  return str.replace(/(?:\[(.+?)\])?\{@class +(.+?)\}/gi,
    function (match, content, longname) {
      const fqclassname = longname.replace(/\//g, '.')
      const classname = fqclassname.replace(/.*\.([^.]+)$/, '$1')
      return self.resolveTypes(fqclassname, classname)
    }
  )
}

function resolveTypes (type, content) {
  const self = this
  if (type == null || type === undefined) return type
  return String(type).split(',')
    .map(function (type) {
      type = type.trim()
      const urlPrefix = matchingUrlPrefixForType(type)
      if (urlPrefix) {
        const classname = type.replace(/.*\.([^.]+)$/, '$1')
        const url = urlPrefix + type.replace(/\./g, '/') + '.html'
        type = util.format('<a href="%s" alt="%s" target="javadoc">%s</a>',
          url, type, classname)
      } else {
        const res = self.data({ name: type })
        if (res.count() < 1) return type
        const kind = res.first().kind
        switch (kind) {
          case 'class':
          case 'mixin':
          case 'module':
          case 'namespace':
            return util.format('<a href="../%ss/%s" alt="%s">%s</a>',
              kind, safeHtmlFilename(type), type, type)
          default:
            return type
        }
      }
      return type
    })
    .join(' | ')
}

/**
 * Builds the list of property links as <a ...> elements
 */
function buildPropertyLinks (doclet) {
  return propertyNames(doclet).map(function (propertyName) {
    return util.format('<a href="#%s">%s</a>', doclet.name, propertyName)
  }).join(', ')
}

/**
 * Builds the property summary from the @summary tag resolves links
 * in the tag value.
 */
function buildPropertySummary (doclet) {
  return this.resolveClassReferences(doclet.summary || '')
}

/**
 * Creates the list of doclet names including the canonical name and
 * a list of optional aliases
 */
function propertyNames (doclet) {
  let names = []
  names.push(doclet.name)
  if (doclet.alias) {
    names = names.concat(doclet.alias)
  }
  return names
}
/**
 * Build the title string for a doc page of a type (a class, a mixin,
 * a namespace, or a module).
 *
 * @param doclet the doclet representing the type
 * @returns {String}
 */
function buildTitleForType (doclet) {
  switch (doclet.kind) {
    case 'class': return 'Class ' + doclet.name
    case 'mixin': return 'Mixin ' + doclet.name
    case 'namespace': return 'Namespace ' + doclet.name
    case 'module': return 'Module ' + doclet.name
    default: return ''
  }
}

function ViewHelper (data) {
  this.data = data
}

ViewHelper.prototype.resolveClassReferences = resolveClassReferences
ViewHelper.prototype.resolveTypes = resolveTypes
ViewHelper.prototype.buildPropertySummary = buildPropertySummary
ViewHelper.prototype.buildPropertyLinks = buildPropertyLinks
ViewHelper.prototype.buildTitleForType = buildTitleForType

exports.ViewHelper = ViewHelper
exports.safeHtmlFilename = safeHtmlFilename
