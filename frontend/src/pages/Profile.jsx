import { useState } from 'react'
import { Bell, BriefcaseBusiness, Check, Mail, MapPin, Pencil, Phone, ShieldCheck, UserCircle2, X } from 'lucide-react'
import InfoTooltip from '../components/InfoTooltip'

const INITIAL_PROFILE = {
  name: 'John Doe',
  role: 'Senior Software Engineer',
  memberSince: 'Mar 2023',
  email: 'john.doe@example.com',
  phone: '+1 (555) 123-4567',
  location: 'New York, NY',
  investorType: 'Balanced Growth',
  riskTolerance: 'Medium',
  timeHorizon: '7-10 years',
  primaryGoal: 'Long-term wealth creation',
  occupation: 'Software Engineer',
  security: 'Account Protected',
  alerts: 'Price + dividend notifications on',
  kycVerified: true,
  twoFactorEnabled: true,
}

const INPUT_CLASSES = 'mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-slate-400'

export default function Profile() {
  const [profile, setProfile] = useState(INITIAL_PROFILE)
  const [draft, setDraft] = useState(INITIAL_PROFILE)
  const [isEditing, setIsEditing] = useState(false)

  const accountDetails = [
    { key: 'email', label: 'Email', value: profile.email, icon: Mail },
    { key: 'phone', label: 'Phone', value: profile.phone, icon: Phone },
    { key: 'location', label: 'Location', value: profile.location, icon: MapPin },
  ]

  const investmentProfile = [
    { key: 'investorType', label: 'Investor Type', value: profile.investorType },
    { key: 'riskTolerance', label: 'Risk Tolerance', value: profile.riskTolerance },
    { key: 'timeHorizon', label: 'Time Horizon', value: profile.timeHorizon },
    { key: 'primaryGoal', label: 'Primary Goal', value: profile.primaryGoal },
  ]

  const handleDraftChange = (key, value) => {
    setDraft((current) => ({ ...current, [key]: value }))
  }

  const handleSave = () => {
    setProfile(draft)
    setIsEditing(false)
  }

  const handleCancel = () => {
    setDraft(profile)
    setIsEditing(false)
  }

  return (
    <div className="space-y-6">
      <div className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="rounded-2xl bg-slate-100 p-4 text-slate-500">
              <UserCircle2 size={36} />
            </div>
            <div>
              {isEditing ? (
                <div className="space-y-2">
                  <input className={INPUT_CLASSES} value={draft.name} onChange={(event) => handleDraftChange('name', event.target.value)} />
                  <input className={INPUT_CLASSES} value={draft.role} onChange={(event) => handleDraftChange('role', event.target.value)} />
                  <input className={INPUT_CLASSES} value={draft.memberSince} onChange={(event) => handleDraftChange('memberSince', event.target.value)} />
                </div>
              ) : (
                <>
                  <div className="flex items-center gap-1.5">
                    <h2 className="text-xl font-semibold text-slate-900">{profile.name}</h2>
                    <InfoTooltip title="Profile overview" description="Your basic account identity details shown across the app." />
                  </div>
                  <p className="text-sm text-slate-500">{profile.role} · Member since {profile.memberSince}</p>
                </>
              )}
            </div>
          </div>
          {isEditing ? (
            <div className="flex gap-2">
              <button type="button" onClick={handleSave} className="inline-flex items-center gap-1.5 rounded-full bg-emerald-700 px-3 py-2 text-xs font-semibold text-white hover:bg-slate-800">
                <Check size={14} />
                Save
              </button>
              <button type="button" onClick={handleCancel} className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100">
                <X size={14} />
                Cancel
              </button>
            </div>
          ) : (
            <button type="button" onClick={() => setIsEditing(true)} className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100">
              <Pencil size={14} />
              Edit Profile
            </button>
          )}
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
            {profile.kycVerified ? 'KYC Verified' : 'KYC Pending'}
          </span>
          <span className="rounded-full border border-indigo-200 bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">
            {profile.twoFactorEnabled ? '2-Step Authentication Enabled' : '2-Step Authentication Disabled'}
          </span>
        </div>
      </div>

      <div className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
        <div className="mb-4 flex items-center gap-1.5">
          <h3 className="text-base font-semibold text-slate-900">Account Details</h3>
          <InfoTooltip title="Account details" description="Primary contact and location details used for your profile." />
        </div>
        <div className="space-y-3">
          {accountDetails.map(({ key, label, value, icon: Icon }) => (
            <div key={label} className="flex items-start gap-3 rounded-2xl border border-slate-100 bg-slate-50 px-4 py-3">
              <div className="mt-0.5 rounded-xl border border-slate-200 bg-white p-2 text-slate-500">
                <Icon size={15} />
              </div>
              <div className="w-full">
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">{label}</p>
                {isEditing ? (
                  <input className={INPUT_CLASSES} value={draft[key]} onChange={(event) => handleDraftChange(key, event.target.value)} />
                ) : (
                  <p className="mt-0.5 text-sm font-medium text-slate-700">{value}</p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="rounded-[24px] border border-slate-200 bg-white p-6 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
        <div className="mb-4 flex items-center gap-1.5">
          <h3 className="text-base font-semibold text-slate-900">Investment Preferences</h3>
          <InfoTooltip title="Investment preferences" description="Your investing style and goals used for personalized context." />
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          {investmentProfile.map(({ key, label, value }) => (
            <div key={label} className="rounded-2xl border border-slate-100 bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">{label}</p>
              {isEditing ? (
                <input className={INPUT_CLASSES} value={draft[key]} onChange={(event) => handleDraftChange(key, event.target.value)} />
              ) : (
                <p className="mt-1.5 text-sm font-medium text-slate-700">{value}</p>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        <div className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <p className="text-sm font-medium text-slate-500">Occupation</p>
              <InfoTooltip title="Occupation" description="Your current role, useful for personal account context." />
            </div>
            <BriefcaseBusiness size={18} className="text-slate-400" />
          </div>
          {isEditing ? (
            <input className={INPUT_CLASSES} value={draft.occupation} onChange={(event) => handleDraftChange('occupation', event.target.value)} />
          ) : (
            <p className="mt-4 text-lg font-semibold text-slate-900">{profile.occupation}</p>
          )}
        </div>
        <div className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <p className="text-sm font-medium text-slate-500">Security</p>
              <InfoTooltip title="Security status" description="Shows the current protection status of your account." />
            </div>
            <ShieldCheck size={18} className="text-slate-400" />
          </div>
          {isEditing ? (
            <input className={INPUT_CLASSES} value={draft.security} onChange={(event) => handleDraftChange('security', event.target.value)} />
          ) : (
            <p className="mt-4 text-lg font-semibold text-slate-900">{profile.security}</p>
          )}
        </div>
        <div className="rounded-[24px] border border-slate-200 bg-white p-5 shadow-[0_10px_30px_rgba(15,23,42,0.05)]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <p className="text-sm font-medium text-slate-500">Alerts</p>
              <InfoTooltip title="Alert preferences" description="Notification settings for price moves and dividend events." />
            </div>
            <Bell size={18} className="text-slate-400" />
          </div>
          {isEditing ? (
            <input className={INPUT_CLASSES} value={draft.alerts} onChange={(event) => handleDraftChange('alerts', event.target.value)} />
          ) : (
            <p className="mt-4 text-lg font-semibold text-slate-900">{profile.alerts}</p>
          )}
        </div>
      </div>
    </div>
  )
}
