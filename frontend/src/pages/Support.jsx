import { useMemo, useState } from 'react'
import { Clock3, ExternalLink, FileText, LifeBuoy, MailQuestion, MessageSquareText, Search } from 'lucide-react'
import Accordion from '../components/Accordion'
import InfoTooltip from '../components/InfoTooltip'
import PageCard from '../components/PageCard'
import SectionHeader from '../components/SectionHeader'
import StatCard from '../components/StatCard'
import { faqItems } from '../data/faq'
import { supportChannels } from '../data/supportChannel'

const ICON_BY_CHANNEL = {
  mail: MailQuestion,
  chat: MessageSquareText,
  docs: FileText,
}

export default function Support() {
  const [searchQuery, setSearchQuery] = useState('')
  const [openFaqs, setOpenFaqs] = useState([])

  const filteredFaqs = useMemo(() => {
    const query = searchQuery.trim().toLowerCase()
    if (!query) return faqItems
    return faqItems.filter((item) => (
      `${item.category} ${item.question} ${item.answer}`.toLowerCase().includes(query)
    ))
  }, [searchQuery])

  const groupedFaqs = useMemo(() => {
    const preferredOrder = ['Getting Started', 'Data & Security', 'Tax & Reports']
    const grouped = filteredFaqs.reduce((acc, item) => {
      const category = item.category || 'General'
      acc[category] = [...(acc[category] || []), item]
      return acc
    }, {})

    return preferredOrder
      .filter((category) => grouped[category]?.length)
      .map((category) => ({ category, items: grouped[category] }))
      .concat(
        Object.keys(grouped)
          .filter((category) => !preferredOrder.includes(category))
          .map((category) => ({ category, items: grouped[category] })),
      )
  }, [filteredFaqs])

  const toggleFaq = (faqKey) => {
    setOpenFaqs((current) => {
      if (current.includes(faqKey)) {
        return current.filter((item) => item !== faqKey)
      }
      return [faqKey, ...current].slice(0, 2)
    })
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard title="Available FAQs" value={String(faqItems.length)} icon={LifeBuoy} subtle info={{ title: 'Available FAQs', description: 'Total number of help answers currently available on this page.' }} />
        <StatCard title="Support Channels" value={String(supportChannels.length)} icon={MessageSquareText} subtle info={{ title: 'Support channels', description: 'Different ways to contact support based on your issue.' }} />
        <StatCard title="Typical Resolution" value="< 24 hours" icon={Clock3} subtle info={{ title: 'Typical resolution', description: 'Average time support takes to respond to common requests.' }} />
      </div>

      <PageCard className="p-5">
        <SectionHeader
          title="Frequently Asked Questions"
          description="Quick answers to common questions about holdings, pricing, and portfolio workflows."
          countLabel={`${filteredFaqs.length} items`}
          info={{
            title: 'FAQ section',
            description: 'Find quick answers to common product questions before contacting support.',
          }}
        />
        <div className="mb-5">
          <label htmlFor="faq-search" className="sr-only">Search FAQs</label>
          <div className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2">
            <Search size={16} className="text-slate-500" />
            <input
              id="faq-search"
              type="text"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search FAQs by keyword"
              className="w-full bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-500"
            />
          </div>
        </div>

        {groupedFaqs.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-6 text-center">
            <p className="text-sm font-semibold text-slate-800">No results for "{searchQuery.trim()}"</p>
            <p className="mt-1 text-sm text-slate-600">Try a different keyword or contact support below.</p>
          </div>
        ) : (
          <div className="space-y-5">
            {groupedFaqs.map(({ category, items }) => (
              <section key={category} className="space-y-2.5">
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">{category}</p>
                <Accordion
                  items={items.map((item, index) => ({
                    key: `${category}-${item.question}-${index}`.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
                    title: item.question,
                    content: item.answer,
                  }))}
                  openKeys={openFaqs}
                  onToggle={toggleFaq}
                />
              </section>
            ))}
          </div>
        )}
      </PageCard>

      <PageCard className="p-5">
        <SectionHeader
          title="Contact Support"
          description="Choose the best channel based on your issue type."
          countLabel={`${supportChannels.length} channels`}
          info={{
            title: 'Contact support',
            description: 'Pick the channel that best matches your request for faster help.',
          }}
        />
        <div className="grid gap-3 xl:grid-cols-3">
          {supportChannels.map(({ title, description, value, icon, href }) => {
            const Icon = ICON_BY_CHANNEL[icon] || MailQuestion
            const isDocumentation = icon === 'docs'
            return (
            <a
              key={title}
              href={href}
              target={isDocumentation ? '_blank' : undefined}
              rel={isDocumentation ? 'noreferrer noopener' : undefined}
              className="group block rounded-2xl border border-slate-200 bg-slate-50 p-4 text-slate-800 transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              <div className="flex items-start gap-3">
                <div className="rounded-xl border border-slate-200 bg-white p-2 text-slate-600">
                  <Icon size={16} />
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <p className="text-sm font-semibold text-slate-900">{title}</p>
                    <InfoTooltip title={title} description={description} />
                  </div>
                  <p className="mt-1 text-sm text-slate-600">{description}</p>
                  <p className="mt-2 inline-flex items-center gap-1.5 text-sm font-semibold text-slate-800">
                    {value}
                    {isDocumentation ? <ExternalLink size={14} /> : null}
                  </p>
                </div>
              </div>
            </a>
            )
          })}
        </div>
      </PageCard>
    </div>
  )
}
