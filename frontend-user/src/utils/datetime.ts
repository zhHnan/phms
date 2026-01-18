export type DateLike = string | number | Date | null | undefined

const pad2 = (n: number) => String(n).padStart(2, '0')

const toDate = (input: DateLike): Date | null => {
    if (input === null || input === undefined) return null
    if (input instanceof Date) return Number.isNaN(input.getTime()) ? null : input

    if (typeof input === 'number') {
        // 兼容秒级时间戳
        const ms = input < 1e12 ? input * 1000 : input
        const d = new Date(ms)
        return Number.isNaN(d.getTime()) ? null : d
    }

    const raw = String(input).trim()
    if (!raw) return null

    // 纯数字字符串：当作时间戳
    if (/^\d+$/.test(raw)) {
        const n = Number(raw)
        return toDate(n)
    }

    // 兼容后端常见的 "YYYY-MM-DD HH:mm:ss" / "YYYY-MM-DD HH:mm"
    // Safari 对带空格的日期字符串解析较差，这里转换成 ISO 风格
    const normalized = raw.includes(' ') && !raw.includes('T')
        ? raw.replace(' ', 'T')
        : raw

    const d = new Date(normalized)
    return Number.isNaN(d.getTime()) ? null : d
}

export const formatDateTime = (input: DateLike): string => {
    if (input === null || input === undefined) return ''

    // 如果是形如 2026-01-12 这种纯日期，直接返回（避免被时区解析成前一天）
    if (typeof input === 'string') {
        const raw = input.trim()
        if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) return raw
    }

    const d = toDate(input)
    if (!d) return typeof input === 'string' ? input : ''

    const y = d.getFullYear()
    const m = pad2(d.getMonth() + 1)
    const day = pad2(d.getDate())
    const hh = pad2(d.getHours())
    const mm = pad2(d.getMinutes())
    return `${y}-${m}-${day} ${hh}:${mm}`
}
