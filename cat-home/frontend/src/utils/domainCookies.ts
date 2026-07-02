const FREQUENT_DOMAIN_COOKIE = 'CAT_DOMAINS'

export function getFrequentDomains(currentDomain?: string) {
  const domains: string[] = []
  const seen = new Set<string>()

  for (const value of readCookieValues(FREQUENT_DOMAIN_COOKIE)) {
    for (const domain of parseDomainCookie(value)) {
      if (!seen.has(domain)) {
        seen.add(domain)
        domains.push(domain)
      }
    }
  }
  if (domains.length) {
    return domains
  }
  return currentDomain ? [currentDomain] : []
}

function readCookieValues(name: string) {
  if (typeof document === 'undefined') {
    return []
  }
  const prefix = `${name}=`

  return document.cookie
    .split(/;\s*/)
    .filter((entry) => entry.startsWith(prefix))
    .map((entry) => entry.substring(prefix.length))
}

function parseDomainCookie(value: string) {
  const normalized = stripQuotes(value.trim())

  return normalized
    .split(/\||%7c/i)
    .map((item) => decodeCookieValue(stripQuotes(item.trim())))
    .filter(Boolean)
}

function stripQuotes(value: string) {
  if (value.length >= 2 && value.startsWith('"') && value.endsWith('"')) {
    return value.substring(1, value.length - 1)
  }
  return value
}

function decodeCookieValue(value: string) {
  try {
    return decodeURIComponent(value).trim()
  } catch {
    return value.trim()
  }
}
