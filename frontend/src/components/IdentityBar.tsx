import type {Identity,RoleCode} from '../types';
const options:Record<string,Identity>={
 applicant:{uuid:'applicant-1',userName:'9990000001',tenantId:'dehradun',roles:[{code:'APPLICANT'}]},
 verifier:{uuid:'verifier-1',userName:'verifier',tenantId:'dehradun',roles:[{code:'VERIFIER'}]},
 approver:{uuid:'approver-1',userName:'approver',tenantId:'dehradun',roles:[{code:'APPROVER'}]},
 haridwarVerifier:{uuid:'verifier-haridwar',userName:'verifier-haridwar',tenantId:'haridwar',roles:[{code:'VERIFIER'}]}
};
export function IdentityBar({identity,onChange}:{identity:Identity;onChange:(x:Identity)=>void}){const key=Object.keys(options).find(k=>JSON.stringify(options[k])===JSON.stringify(identity))||'applicant';return <div className="identity"><label>Test caller <select value={key} onChange={e=>onChange(options[e.target.value])}><option value="applicant">Applicant · Dehradun</option><option value="verifier">Verifier · Dehradun</option><option value="approver">Approver · Dehradun</option><option value="haridwarVerifier">Verifier · Haridwar</option></select></label><span>{identity.tenantId} · {identity.roles.map(r=>r.code as RoleCode).join(', ')}</span></div>}
