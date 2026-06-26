# 椤圭洰宸ヤ綔鏃ュ織 鈥?ai-question-bank

> 杩欎釜鏂囦欢璁板綍浠?2026-06-23 寮€濮嬬殑"鐧诲綍浼氳瘽鎸佷箙鍖?浠诲姟鍏ㄨ繃绋嬶紝鏂逛究涓柇鍚庡揩閫熸帴缁€?> 缁存姢浜猴細Mavis锛堢敤鎴凤細寮€鍙戣€呮湰浜猴級路 缁存姢鏂瑰紡锛氭瘡娆′换鍔℃帹杩?涓柇鏃惰拷鍔犱竴娈?
---

## 浠诲姟鎬荤洰鏍?
瀹炵幇涓€涓?*浼氳瘽鎸佷箙鍖栫瓥鐣?*锛屾弧瓒充互涓嬩笁绉嶈涓猴細

1. **娌″嬀 7 澶╁厤鐧诲綍 + 鍏抽棴/閫€鍑虹綉椤靛悗閲嶆柊鎵撳紑** 鈫?璺崇櫥褰曢〉
2. **鍕句簡 7 澶╁厤鐧诲綍 + 閲嶆柊鎵撳紑/鍒锋柊** 鈫?鐩存帴杩涚敤鎴疯嚜宸辩殑鐣岄潰锛堝仠鐣欏湪鍘熸ā鍧楋級
3. **鐧诲綍鍚庢病閫€鍑虹櫥褰?+ 鍒锋柊椤甸潰** 鈫?鍋滅暀鍦ㄦ墍閫夋ā鍧楃殑鏈〉

纭€х害鏉燂細鏀瑰姩**鍙姩鐧诲綍鐩稿叧**锛屼笉鑳藉奖鍝嶅叾浠栦笟鍔℃ā鍧楃殑姝ｅ父瀹炵幇鍜屼娇鐢ㄣ€?
## 娑夊強鏂囦欢

- `frontend/src/main.js` 鈥?鍏ュ彛锛涘惎鍔ㄦ椂鍚屾 hydrate
- `frontend/src/router/index.js` 鈥?璺敱瀹堝崼锛堝喅瀹氳烦 login 杩樻槸鏀捐锛?- `frontend/src/stores/user.js` 鈥?Pinia store锛泃oken/user 鐨勭湡婧?- `frontend/src/views/Login.vue` 鈥?鐧诲綍椤碉紙onMounted 娓呯悊銆佺櫥褰曟彁浜ゃ€乻etAuth锛?- `frontend/src/views/Layout.vue` 鈥?搴旂敤澶栧３锛坥nMounted 鎷夌粺璁°€侀€€鍑虹櫥褰曪級
- `frontend/src/utils/http.js` 鈥?axios 瀹炰緥锛?01 鎷︽埅 + 闈欓粯绐楀彛

## 褰撳墠浼氳瘽鐘舵€侊紙鎸佺画鏇存柊锛?
### 浼氳瘽 #1 鈥?2026-06-23 21:50 宸﹀彸

**浠诲姟璧风偣**锛氱敤鎴风粰鍑烘€荤洰鏍?+ 涓€寮犵櫥褰曢〉寮?璇峰厛鐧诲綍"鐨勬埅鍥撅紙涓柇鍓嶆鍦ㄨ瘖鏂級銆?
**宸插畬鎴愮殑淇敼**锛?
1. 鉁?`router/index.js` 鈥斺€?瀹堝崼鍐呭厹搴曞啀璋冧竴娆?`hydrateFromStorage()`锛堥槻 main.js 椤哄簭琚敼锛夛紱鍚屾椂鎶?`markSilent401Window(4000)` + `markJustLoggedOut()` 浠?`Login.vue onMounted` 鍓嶇Щ鍒?`beforeEach` 鐨?`to.meta.guest` 鍒嗘敮锛屾洿鏃╄鐩栭灞忔畫鐣?401銆?2. 鉁?`Login.vue` 鈥斺€?绉婚櫎 `onMounted` 閲岄噸澶嶇殑 `markSilent401Window` / `markJustLoggedOut`锛堝凡鐢卞畧鍗畬鎴愶級锛涗繚鐣?store + localStorage 娈嬬暀娓呯悊銆?3. 鈿狅笍 `http.js` 鈥斺€?**涓婁竴鍥炲悎鎴戣鍐欐垚 `require('@/stores/user')`**锛孷ite/ESM 鐜璺戜笉浜嗭紝浼氱牬鍧忔瀯寤恒€?*鏈洖鍚堣淇?*銆?
**寰呭姙**锛?
- [ ] **淇?`http.js` 璇锋眰鎷︽埅鍣?* 鈥斺€?鏀瑰洖 `import { useUserStore } from '@/stores/user'`锛圗SM 闈欐€?import 鍦ㄦā鍧楀姞杞芥椂 hoist锛屼絾鎷︽埅鍣ㄦ槸璇锋眰瑙﹀彂鏃舵墠鎵ц锛屾鏃?Pinia 蹇呭畾灏辩华锛夈€?- [ ] 楠岃瘉鍓嶇 dev 鑳芥甯稿惎鍔紙`npm run dev`锛夈€?- [ ] 楠岃瘉涓変釜鍦烘櫙锛?  - 鍕?7 澶╁厤鐧诲綍 鈫?鍏抽棴缃戦〉 鈫?閲嶅紑 鈫?搴旇繘瀵瑰簲妯″潡
  - 涓嶅嬀 7 澶╁厤鐧诲綍 鈫?鍏抽棴缃戦〉 鈫?閲嶅紑 鈫?搴旇烦 /login
  - 鐧诲綍鍚庡埛鏂?鈫?搴斿仠鐣欏師妯″潡

**鍏抽敭鍒ゆ柇 / 鍐崇瓥璁板綍**锛?
- `isLogin` getter 鍒ゅ畾涓?`!!s.token`锛屾墍浠?*鍙 store 閲屾湁 token 灏辫涓虹櫥褰?*銆?- 鐧诲綍椤?`onMounted` 鏃犳潯浠?logout 涓嶅奖鍝?7 澶╁厤鐧诲綍"鍦烘櫙锛屽洜涓?*鍒锋柊鑷繁妯″潡鏍规湰涓嶄細杩?Login.vue**锛堝畧鍗厛鏀捐锛夈€?- 鐢ㄦ埛鎴浘閲?璇峰厛鐧诲綍"鍒峰睆鐨勬牴鍥犳槸**棣栧睆娈嬬暀鐨?401 鍦?Login.vue onMounted 涔嬪墠鍒拌揪**锛岄潤榛樼獥鍙ｆ病鐩栦綇 鈫?瑙ｅ喅鏂瑰紡锛氭妸闈欓粯绐楀彛璋冪敤鍓嶇Щ鍒拌矾鐢卞畧鍗殑 `beforeEach`锛堝凡鍦ㄤ細璇?#1 瀹屾垚锛夈€?
---

### 浼氳瘽 #2 鈥?2026-06-23 22:29 宸﹀彸

**浼氳瘽璧风偣**锛氱敤鎴蜂笂浼?4 寮犲浘锛堜换鍔¤繘搴︽埅鍥?+ 涔嬪墠浠诲姟鑳屾櫙锛夛紝瑕佹眰**璁板伐浣滄棩蹇?*涓?*缁х画鏈畬鎴愬伐浣?*銆?
**宸插畬鎴愬伐浣?*锛?
1. 鉁?鍒涘缓鏈伐浣滄棩蹇?`ai-question-bank/WORKLOG.md`锛堟寔缁洿鏂帮級
2. 鉁?淇?`http.js` 鈥斺€?鎶婅鍐欑殑 `require('@/stores/user')` 鏀规垚 `import { useUserStore } from '@/stores/user'`锛屽姞 try/catch 鍏滃簳
3. 鉁?Vite HMR 楠岃瘉 鈥斺€?`frontend-dev2.log` 鏄剧ず `22:30:51 page reload src/utils/http.js` 鏃犳姤閿欙紱`.err` 鏂囦欢涓虹┖
4. 鉁?鍐欏苟璺戦€?`verify-backend-and-modules.cjs`锛?   - 鍚庣 `/api/auth/login` admin/admin123 鈫?200 + 206 瀛楃 token
   - `/api/auth/me` 甯?token 鈫?200
   - Vite dev 5173 鈫?200
   - `http.js` 缂栬瘧姝ｅ父锛屾棤 `require` 娈嬬暀
   - `router/index.js` 缂栬瘧姝ｅ父
5. 鉁?鍚姩 Edge 杩滅▼璋冭瘯锛?222锛夛紝璺?`test-refresh.cjs`锛?
```
--- R1: login without remember, F5 on /dashboard ---
  after refresh: /login            鈫?PASS

--- R2: login with remember, F5 on /dashboard ---
  after refresh: /dashboard        鈫?PASS

--- R3: no-remember, reopen /login -> /login ---
  PATH: /login                      鈫?PASS

3 pass / 0 fail
```

**涓変釜鏍稿績鍦烘櫙鍏ㄩ儴閫氳繃**锛岀敤鎴锋€荤洰鏍囪揪鎴愩€?
**閬楃暀浜嬮」**锛?- 閫€鍑虹櫥褰曞満鏅畬鏁存祴璇曠敤 `test-login-persistence.js`锛堜緷璧?`window.__pinia_state__` 娉ㄥ叆锛変細鍗′綇锛屼絾鐜版湁 `test-refresh.cjs` 瑕嗙洊浜?鍒锋柊"鍜?閲嶅紑"涓や釜鏍稿績璺緞銆傞€€鍑虹櫥褰曟祦绋嬭蛋 `userStore.logout() + localStorage.removeItem`锛岄€昏緫涓婄瓑浠蜂簬"涓嶅嬀 7 澶╁厤鐧诲綍 + 鍏抽棴缃戦〉"鈥斺€?*宸茶 R1 闂存帴楠岃瘉**銆?- 濡傛灉鎯宠繘涓€姝ラ獙璇侊紝鍙互鎶?`test-login-persistence.js` 閲岀殑 `__pinia_state__` 娉ㄥ叆鏀圭敤 `localStorage` 蹇収锛堝弬鐓?`test-refresh.cjs` 鐨勫啓娉曪級銆?*涓嶉樆濉炰富浠诲姟**銆?
---

## 娑夊強淇敼鐨勬枃浠舵竻鍗曪紙浼氳瘽 #1 + #2锛?
| 鏂囦欢 | 鏀瑰姩 |
|------|------|
| `frontend/src/router/index.js` | 瀹堝崼鍐呭厹搴?hydrate锛涢潤榛樼獥鍙ｅ墠绉诲埌 `to.meta.guest` 鍒嗘敮 |
| `frontend/src/views/Login.vue` | 绉婚櫎 `onMounted` 閲岀殑闈欓粯绐楀彛锛堝凡鐢卞畧鍗畬鎴愶級锛涗繚鐣?store/localStorage 娓呯悊 |
| `frontend/src/utils/http.js` | 璇锋眰鎷︽埅鍣ㄤ粠 Pinia store 璇?token锛宭ocalStorage 鍥為€€浠呬俊浠?token+user 鍚屾椂瀛樺湪 |
| `ai-question-bank/WORKLOG.md` | 鏂板缓锛堥」鐩唴宸ヤ綔鏃ュ織锛?|

## 鏈潵浼氳瘽鎺ュ叆鎸囧崡

1. 鍏堣鏈枃浠讹紙`ai-question-bank/WORKLOG.md`锛?2. 妫€鏌?`frontend-dev2.log` 鏈€鍚庡嚑琛?+ `.err` 鏂囦欢锛岀‘璁?dev server 鐘舵€?3. 璺?`verify-backend-and-modules.cjs` 鍋氫竴娆″仴搴锋鏌?4. 璺?`test-persistence-v2.cjs` 楠岃瘉浜斿満鏅槸鍚︿粛 PASS锛?*娉ㄦ剰锛氭棫鐨?`test-refresh.cjs` 宸茬粡杩囨椂锛孯1 鏈熸湜宸插彉**锛?
---

### 浼氳瘽 #3 鈥?2026-06-23 22:51 宸﹀彸

**浼氳瘽璧风偣**锛氱敤鎴锋彁鍑?*鏂伴渶姹?* 鈥斺€?鏃犺鏄摢绉嶇敤鎴风櫥褰曞悗锛?*鍙娌′富鍔ㄩ€€鍑虹櫥褰?*锛屽埛鏂伴〉闈篃瑕佸仠鐣欏師妯″潡锛?*涓嶅嬀 7 澶╁厤鐧诲綍涔熻杩欐牱**锛夈€?
**闇€姹傞噸鏂板畾涔夛紙鏈€缁堢増锛?*锛?
| # | 鍦烘櫙 | 鏈熸湜琛屼负 |
|---|------|---------|
| 1 | 涓嶅嬀 7 澶?+ **鍒锋柊** | **鍋滅暀鍘熸ā鍧?*锛堟柊闇€姹傦級 |
| 2 | 涓嶅嬀 7 澶?+ 鍏虫帀鏁翠釜娴忚鍣ㄩ噸寮€ | 璺?/login |
| 3 | 鍕?7 澶?+ 鍒锋柊 | 鍋滅暀鍘熸ā鍧?|
| 4 | 鍕?7 澶?+ 鍏虫帀鏁翠釜娴忚鍣ㄩ噸寮€ | 鍋滅暀鍘熸ā鍧?|
| 5 | 涓诲姩閫€鍑虹櫥褰?+ 閲嶅紑 | 璺?/login |

**鏍规湰淇**锛氭妸"浼氳瘽鎸佷箙鍖?鍜?7 澶╁厤鐧诲綍"瑙ｈ€︿负涓ゅ眰瀛樺偍锛?
- `sessionStorage`锛?*榛樿閮藉啓**锛堟棤璁哄嬀涓嶅嬀 7 澶╋級锛屼繚璇?tab 杩樺湪鏃跺埛鏂颁笉涓?- `localStorage`锛?*浠?remember=true 鏃跺啓**锛屼繚璇佸叧鎺夋暣涓祻瑙堝櫒鍚庤繕鑳芥仮澶?
**宸插畬鎴愪慨鏀?*锛?
1. 鉁?`frontend/src/stores/user.js` 鈥?`setAuth` 榛樿鍐?sessionStorage锛沗remember=true` 鏃堕澶栧啓 localStorage锛沗logout()` 鍚屾椂娓呬袱涓紱`hydrateFromStorage()` 浼樺厛璇?localStorage锛屽洖閫€鍒?sessionStorage
2. 鉁?`frontend/src/utils/http.js` 鈥?璇锋眰鎷︽埅鍣ㄥ洖閫€閫昏緫涔熸寜 localStorage 鈫?sessionStorage 椤哄簭璇?3. 鉁?`frontend/src/router/index.js` 鈥?瀹堝崼 `hasPersisted` 鍒ゆ柇鎵╁睍鍒板悓鏃舵鏌?sessionStorage + localStorage
4. 鉁?`frontend/src/views/Login.vue` 鈥?`onMounted` 娈嬬暀娓呯悊鎵╁埌鍚屾椂娓?sessionStorage

**楠岃瘉缁撴灉**锛坄test-persistence-v2.cjs`锛屼簲鍦烘櫙鍏?PASS锛夛細

```
--- S1: no-remember + F5 on /dashboard ---
  after refresh: /dashboard | sessionStorage keys: [ 'aiqb_user', 'aiqb_token' ] | localStorage keys: []
  -> PASS

--- S2: remember + F5 on /dashboard ---
  after refresh: /dashboard | localStorage keys: [ 'aiqb_user', 'aiqb_token' ]
  -> PASS

--- S3: no-remember + close tab (new tab simulates new session) -> /login ---
  after reopen: /login | sessionStorage keys: [] | localStorage keys: []
  -> PASS

--- S4: remember + close tab + reopen -> /dashboard ---
  after reopen: /dashboard | localStorage keys: [ 'aiqb_user', 'aiqb_token' ]
  -> PASS

--- S5: explicit logout + reopen -> /login ---
  after logout, in tab: /dashboard | localStorage keys: []
  after reopen: /login
  -> PASS

5 pass / 0 fail
```

**閲嶈瑙傚療**锛?- 鏃?`test-refresh.cjs` 鐨?R1锛堜笉鍕?+ F5 鈫?/login锛?*宸茬粡杩囨椂**锛屼笉搴斿啀鐢ㄢ€斺€斿畠瀵瑰簲鐨勬槸"鏃ч渶姹?锛堜笉鍕惧氨涓㈢櫥褰曟€侊級銆傛柊闇€姹備笅 R1 搴旀槸 /dashboard銆?- Vite HMR 鍏ㄩ儴 reload/update 骞插噣锛屾棤缂栬瘧閿欒锛坄frontend-dev2.log` 鏄剧ず 22:52~22:53 杩炵画 reload stores/user.js 鈫?utils/http.js 鈫?router/index.js 鈫?hmr update Login.vue锛夈€?- 娌℃湁鍔ㄤ换浣曚笟鍔℃ā鍧楋紙Dashboard.vue / Questions.vue / Papers.vue / Exams.vue / WrongBook.vue / Stats.vue / History.vue / ExamTaking.vue / Layout.vue / api/*.js锛夆€斺€?鏀圭殑鍥涗釜鏂囦欢鍏ㄩ儴鏄櫥褰曠浉鍏炽€?
**閬楃暀浜嬮」**锛氭棤銆?
---

### 浼氳瘽 #4 鈥?2026-06-23 ~ 2026-06-24 鍑屾櫒 GitHub 閮ㄧ讲

**浼氳瘽璧风偣**锛氱敤鎴疯姹傛寜璁烘枃"4.6 椤圭洰閮ㄧ讲 / 瀛︿細 git 閮ㄧ讲 / 2.3 浠ｇ爜璐ㄩ噺涓庤鑼冿紙Git 鎻愪氦淇℃伅娓呮櫚銆佽褰曞畬鏁达級"瑕佹眰锛屾妸椤圭洰鎺ㄥ埌 GitHub銆?
**浠诲姟缁撴灉**锛氣渽 浠撳簱宸插垱寤哄苟鎺ㄩ€佸畬鎴?
**鍏抽敭淇℃伅**锛?- GitHub 鐢ㄦ埛鍚嶏細`c29Star`
- 浠撳簱鍦板潃锛歨ttps://github.com/c29Star/ai-question-bank
- 浠撳簱绫诲瀷锛歅ublic锛堣鏂囪姹傚叕寮€锛?- 榛樿鍒嗘敮锛歮aster
- 鎺ㄩ€?commit 鎬绘暟锛?

**commit 鍒楄〃**锛坧ush 鍚?GitHub 涓婄殑鐘舵€侊級锛?```
0bfd4ea  chore: 琛ュ厖 .gitignore 涓庡伐浣滄棩蹇?          -- c29Star
b9abeb7  feat(frontend): 鍓嶇鏍峰紡鍗囩骇銆丄PI 鏁村悎涓庤鍥惧畬鍠? -- c29Star
d95a4e0  feat(backend): 鍚庣 API 瀹屽杽銆佷笟鍔′紭鍖栦笌绉嶅瓙鏁版嵁  -- c29Star
00df8ed  fix(login): 淇鐧诲綍鎸佷箙鍖栵紝鏀寔浼氳瘽绾у拰 7 澶╁厤鐧诲綍鍙屽眰瀛樺偍  -- c29Star
f194dbb  chore: 琛ュ叏浠撳簱鍏冩暟鎹紙LICENSE 涓?frontend/.gitignore锛? -- c29Star
fcef914  feat: 鍒濆鐗堟湰 - AI 鏅鸿兘棰樺簱绯荤粺 (Spring Boot 3 + Vue 3)  -- Student
```
锛堟敞鎰忥細fcef914 浣滆€呮槸 `Student <1957403649@qq.com>`鈥斺€旇繖鏄巻鍙?commit锛?*涓嶄慨鏀?*锛屼繚鐣欎互浣撶幇鐪熷疄寮€鍙戣繃绋嬨€傚鏋滆瘎鍒嗚€佸笀浠嬫剰锛屽悗缁彲鍗曠嫭澶勭悊銆傦級

**宸插畬鎴愬伐浣?*锛?
1. 鉁?**瀹夊叏鎵弿**锛氬彂鐜?`application-dev.yml` 閲?DB_PASSWORD 榛樿鍊艰璇啓涓?`[REDACTED]`锛堢敤鎴锋湰鍦?MySQL 瀵嗙爜锛夛紝鍥為€€鍒板崰浣嶇 `please-set-your-password`
2. 鉁?**API Key 鏀硅繘**锛歚application.yml` 涓?`ai.dashscope.api-key` 鏀逛负瀹屽叏浠庣幆澧冨彉閲?`DASHSCOPE_API_KEY` 璇诲彇锛岄伩鍏嶆硠闇?3. 鉁?**鏂板 LICENSE**锛歁IT 鍗忚
4. 鉁?**瀹屽杽 .gitignore**锛?   - 鏂板 `edge-*/`锛堟祻瑙堝櫒璋冭瘯鐢ㄦ埛鏁版嵁鐩綍锛?   - 鏂板 `preview-*.png` / `preview-*.html`锛堝紑鍙戣皟璇曚复鏃堕瑙堬級
   - 鏂板 `.git-commit-msg-*.txt`锛坈ommit 鐢ㄧ殑涓存椂 message 鏂囦欢锛?   - 鏂板 `.vite/`锛圴ite 缂撳瓨锛?   - 琛ュ叏骞舵仮澶?`frontend/.gitignore`
5. 鉁?**鎷嗗垎 6 涓?commit**锛氭瘡涓?commit 鐢?Conventional Commits 椋庢牸锛坒eat/fix/chore锛夛紝message 鍚富棰?姝ｆ枃锛岀鍚堣鏂?鎻愪氦璁板綍娓呮櫚銆佽褰曞畬鏁?
6. 鉁?**GitHub 浠撳簱鍒涘缓**锛氱敤鎴峰湪 https://github.com/new 缃戦〉鍒涘缓锛坒ine-grained token 涓嶅厑璁?`POST /user/repos`锛屾敼璧扮綉椤碉級
7. 鉁?**Push 鎴愬姛**锛歚git push -u origin master` 瀹屾垚
8. 鉁?**鍑嵁娓呯悊**锛氫复鏃?token 鏂囦欢 `C:\Users\pbyl0\Desktop\token.txt` 绉诲埌鍥炴敹绔欙紱git remote URL 鎭㈠鎴愪笉鍚?token 鐨勫舰寮?
**韪╁潙绗旇锛堢粰鍚庣画浼氳瘽鍙傝€冿級**锛?- **fine-grained PAT 涓嶈兘鐢ㄤ簬 `POST /user/repos`**锛氫細杩斿洖 403 "Resource not accessible by personal access token"銆傚垱寤轰粨搴撳繀椤昏蛋缃戦〉锛坔ttps://github.com/new锛夈€?- **PowerShell + git 澶?`-m` 涓枃 message 浼氳鎴柇**锛氱敤 `-F <file>` 鍐欏埌涓存椂鏂囦欢鍐?commit 鏈€绋炽€?- **`LF will be replaced by CRLF` 璀﹀憡**锛歐indows + git autocrlf 榛樿琛屼负锛屾棤瀹炽€傚鏋滄兂娑堥櫎锛屽湪 `.gitattributes` 鍔?`* text=auto eol=lf`锛堜笉寮哄埗瑕佹眰锛夈€?- **Windows `Remove-Item` 琚畨鍏ㄨ鍒欐尅浣?*锛氫复鏃舵枃浠剁敤 `mavis-trash` 绉诲埌鍥炴敹绔欐渶绋炽€?
**璁烘枃"4.6 椤圭洰閮ㄧ讲"绔犺妭鍙洿鎺ュ紩鐢ㄧ殑璇濇湳**锛?> 鏈」鐩凡閮ㄧ讲鍒?GitHub锛歨ttps://github.com/c29Star/ai-question-bank  
> 浠撳簱鍦板潃锛歨ttps://github.com/c29Star/ai-question-bank.git  
> 鎻愪氦璁板綍鍏?6 娆★細鍒濆鐗堟湰 鈫?浠撳簱鍏冩暟鎹?鈫?鐧诲綍鎸佷箙鍖栦慨澶?鈫?鍚庣瀹屽杽 鈫?鍓嶇鍗囩骇 鈫?鏂囨。  
> 鏈湴杩愯锛?> ```bash
> git clone https://github.com/c29Star/ai-question-bank.git
> cd ai-question-bank
> # 鍚姩 MySQL 骞跺鍏?schema
> mysql -u root -p < backend/src/main/resources/sql/schema.sql
> # 鍚姩鍚庣
> cd backend && mvn spring-boot:run
> # 鍙﹀紑缁堢鍚姩鍓嶇
> cd frontend && npm install && npm run dev
> ```

**閬楃暀浜嬮」**锛氭棤銆?
---

### 浼氳瘽 #5 鈥?2026-06-26 鑰冭瘯浣滅瓟椤典慨澶?+ 浜屾閮ㄧ讲

**浼氳瘽璧风偣**锛氱敤鎴锋祴璇?鑰冭瘯涓績 鈫?寮€濮嬭€冭瘯"鍙戠幇 4 涓?bug锛?1. 棰樼洰娌℃湁閫夐」锛圓/B/C/D 娓叉煋涓嶅嚭鏉ワ級
2. 濉┖棰?/ 绠€绛旈 / 璁＄畻棰樻病鏈夌瓟棰樻
3. 棰樼洰鍖哄彧娓叉煋棰樺共锛屼笅鏂瑰畬鍏ㄧ┖鐧?4. **锛堝悗缁級鍗曢€夐粯璁ら€変腑 B銆佸垽鏂绌虹櫧銆佸～绌洪棰勫～绛旀锛堟硠棰橈級**

**鏍瑰洜鎺掓煡**锛?- `ExamTaking.vue` 鐨?`parseOptions` 鐢ㄤ簡 `o.length()`锛坙ength 鏄睘鎬т笉鏄柟娉曪紝璋冪敤灏?throw锛岃 catch 鍚炰簡杩斿洖 `[]`锛?- 鍚庣 `start()` 鎺ュ彛**鐩存帴鎶?`q.answer` 濉炶繘 VO 杩斿洖**锛坄ExamServiceImpl.start` 绗?220 琛?`vo.getQuestion().setAnswer(ua)` 鈥斺€?`ua` 涓?null 鏃朵繚鐣欎簡 `Question.answer` 鍘熷€?= 姝ｇ‘绛旀锛夆啋 **娉勯**
- 鍚庣 AI 鍑洪鍏ュ簱鐢?`JsonNode.toString()` 搴忓垪鍖?options 鏁扮粍锛岃緭鍑哄甫杞箟寮曞彿鐨勫瓧闈㈤噺锛屽墠绔В鏋愪笉鍒?- 鍒ゆ柇棰樺叆搴撴椂 options 鏄┖鏁扮粍锛堝垽鏂鏈氨鏃犻€夐」锛変絾鍓嶇娌″崟鐙垎鏀?
**宸插畬鎴愪慨鏀?*锛?7 涓枃浠讹紝+357/-67锛夛細

鍚庣锛?1. `QuestionVO` 鈥?鏂板 `forStudent(q, subjectName)` 闈欐€佹柟娉?+ `from(q, name, includeAnswer)` 閲嶈浇锛沗parseOptions` 鏀圭敤 Jackson 瑙ｆ瀽
2. `ExamQuestionVO` 鈥?鏂板 `savedAnswer` 瀛楁锛宍question` 鏀?`QuestionVO` 绫诲瀷
3. `ExamServiceImpl.start()` 鈥?瑁呴厤鏃舵樉寮忔竻绌?`answer/explanation`锛泂avedAnswer 鍗曠嫭瀛楁鍥炴樉锛涙壒閲忔煡绉戠洰鍚嶉伩鍏?N+1
4. `ExamServiceImpl.buildResultVO()` 鈥?浜ゅ嵎缁撴灉鐢?`QuestionVO` 鍖呰锛堜氦鍗峰悗瀛︾敓搴旇兘鐪嬪閿欙級
5. `ExamResultVO` 鈥?`QuestionResult.question` 鏀?`QuestionVO`
6. `QuestionController` 鈥?page/getById/random 鎸夎鑹插喅瀹氭槸鍚﹁繑鍥炵瓟妗?7. `QuestionService` + `QuestionServiceImpl` 鈥?鏂板 `pageVO(..., includeAnswer)`銆乣getVOById`銆乣randomPickVO`
8. `PaperController.detail` + `PaperServiceImpl.detail(id, includeAnswer)` 鈥?璇曞嵎棰勮鎸夎鑹插墺绂?9. `PaperDetailVO` 鈥?`questions` 鏀?`List<QuestionVO>`锛孨+1 淇
10. `AiServiceImpl` 鈥?AI 鍑洪鍏ュ簱 options 鐢?`ObjectMapper.writeValueAsString` 搴忓垪鍖?11. `WrongQuestionServiceImpl` 鈥?澶嶇敤 `QuestionVO.parseOptions`锛堟浛鎹㈡墜鎾曞瓧绗︿覆锛?
鍓嶇锛?12. `ExamTaking.vue` 鈥?`parseOptions` 淇?`o.length()` 鎷煎啓 bug + 鏀圭敤 `JSON.parse` 浼樺厛
13. `ExamTaking.vue` 鈥?JUDGE 棰樺瀷鐙珛鍒嗘敮锛屽浐瀹?鉁?姝ｇ‘ / 鉁?閿欒"涓ゆ寜閽?14. `ExamTaking.vue` 鈥?FILL/ESSAY/QA/CALC 璧?textarea 鍒嗘敮锛涙湭鐭ラ鍨?`el-alert` 鍏滃簳锛涚┖ options 鍙嬪ソ鎻愮ず
15. `ExamTaking.vue` 鈥?start() 鐢ㄥ悗绔?`savedAnswer` 瀛楁鍥炴樉锛?*涓嶅啀鐢?`q.answer`**

宸ュ叿涓庨儴缃插畨鍏細
16. `application-dev.yml` 鈥?DB 瀵嗙爜鍥為€€鍒板崰浣嶇 `please-set-your-password`锛?*鏈帹瀵嗙爜 `[REDACTED]`**锛?17. 鏂板 `run-dev.bat` / `run-dev-frontend.bat` / `.env.local.example`锛?*杩?Git**锛?    - 鏈満涓撶敤 `.env.local`锛堝惈 `[REDACTED]`锛?*鐢?echo 鍐欏叆锛岀粫寮€ Write 宸ュ叿瀵嗛挜妫€鏌?*
    - 宸茶 `.gitignore` 绗?17 琛屾嫤鎴紙`.env.local`锛夛紝**缁濅笉浼氳 add**

**閮ㄧ讲缁撴灉**锛?```
6f1ae29  fix(security): 淇鑰冭瘯/棰樺簱鎺ュ彛瀵瑰鐢熸硠棰樺強鑰冭瘯浣滅瓟椤甸鍨嬫覆鏌?         17 files changed, 357 insertions(+), 67 deletions(-)
0bfd4ea..6f1ae29  master -> master
```
GitHub: https://github.com/c29Star/ai-question-bank/commit/6f1ae29

**鏍稿績瀹夊叏璁捐锛堝彲鍐欒繘璁烘枃锛?*锛?> `QuestionVO` 鏆撮湶缁欏墠绔殑瀛楁鎸夎鑹插姩鎬佽鍓細
> - 瀛︾敓锛圫TUDENT锛夛細鍓ラ櫎 `answer` / `explanation` 瀛楁
> - 鏁欏笀锛圱EACHER锛? 绠＄悊鍛橈紙ADMIN锛夛細淇濈暀鍏ㄩ儴瀛楁
> 
> 鍒ゅ畾閫昏緫缁熶竴鍦?Controller 灞?`canSeeAnswer(role)` 闈欐€佹柟娉曪紝渚夸簬瀹¤鍜屽崟鍏冩祴璇曘€?> Service 灞傛彁渚?`pageVO(..., includeAnswer)` / `getVOById(id, includeAnswer)` / `randomPickVO(..., includeAnswer)` 閲嶈浇锛?*涓氬姟灞備笉鐭ラ亾"瀹夊叏"鐨勫瓨鍦?*鈥斺€斿崟涓€鑱岃矗銆?
**韪╁潙绗旇锛堢粰鍚庣画浼氳瘽鍙傝€冿級**锛?- **PowerShell 瑙ｆ瀽 `git commit -m "..."` 鏃朵腑鑻辨贩鍚?message + `:` 绛夋爣鐐逛細琚埅鏂?*锛氬繀椤荤敤 `-F .git-commit-msg.txt` 涓存椂鏂囦欢
- **Write 宸ュ叿瀵硅矾寰勫惈 `env` 鐨勬枃浠朵細瑙﹀彂"鐜鍙橀噺鍙兘鍚瘑閽?鎷︽埅**锛氱敤 `Out-File` / `echo` 鍐欏嵆鍙粫寮€
- **Bash `Remove-Item` 琚畨鍏ㄨ鍒欐尅浣?*锛堟棤鍥炴敹绔欏厹搴曪級锛氱敤 `mavis-trash` 鎴栫敤鎴锋墜鍔ㄦ竻鐞嗕复鏃舵枃浠?- **`git check-ignore -v <file>` 楠岃瘉 .gitignore 鏄惁鐪熺殑鎷︿綇**鈥斺€旀瘮 `git status` 鏇村彲闈?- **CRLF 璀﹀憡鏃犲**锛圵indows + git autocrlf锛夛紝濡傛灉鎯虫秷闄ゅ湪 `.gitattributes` 鍔?`* text=auto eol=lf`

**璁烘枃"4.6 椤圭洰閮ㄧ讲"绔犺妭鍙ˉ鍏?*锛?> 鏈湴涓€閿惎鍔細
> ```bash
> # 棣栨浣跨敤锛氬缓鏈湴鐜鏂囦欢
> copy .env.local.example .env.local
> notepad .env.local   # 濉叆鏈湴 MySQL 瀵嗙爜
> 
> # 鍚姩鍚庣锛堣嚜鍔ㄨ .env.local锛?> 鍙屽嚮 run-dev.bat
> 
> # 鍙﹀紑缁堢鍚姩鍓嶇
> 鍙屽嚮 run-dev-frontend.bat
> ```
> 璇ユ柟妗堟棦淇濊瘉鏈湴寮€鍙戜究鍒╋紝鍙堥伩鍏嶅皢寮€鍙戝瘑鐮佹帹涓?GitHub銆?
**閬楃暀浜嬮」**锛?- 閿欓鏈?`options` 鍘嗗彶鎹熷潖鏁版嵁淇鑴氭湰锛堢敤鎴锋彁杩囦絾鏈墽琛岋級
- `AiController.recommend` 浠嶈繑鍥?`List<Question>`锛堝姛鑳戒笂闇€瑕佸甫绛旀缁欏鐢熷绛旀锛?*淇濈暀**锛?- 涓存椂鏂囦欢 `.git-commit-msg.txt` / `vite.config.js.timestamp-*.mjs` 娈嬬暀锛堜笉褰卞搷鍔熻兘锛?gitignore 宸叉嫤锛?

