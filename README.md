# Pop Culture Quiz

**using google translate to scramble up movie quotes**

## setup
install sbt from https://www.scala-sbt.org/

## usage

```sh
sbt "run \"Join the ranks of the legendary force of soldiers, scientists, adventurers and strangeness, known as [blank]\" \"Choose your hero and clash on the battlefields of tomorrow, waste time, ignore the physics and use emergency forces in the final team shooting\"" 
```

will produce an output like:
```sh
translation step: en => ar
Map(Join the ranks of the legendary force of soldiers, scientists, adventurers and strangeness, known as [blank] -> انضم إلى صفوف القوة الأسطورية للجنود والعلماء والمغامرين والغرابة ، والمعروفة باسم [فارغ], Choose your hero and clash on the battlefields of tomorrow, waste time, ignore the physics and use emergency forces in the final team shooting -> اختر بطلك واشتبك في ساحات القتال في الغد ، وضيع الوقت ، وتجاهل الفيزياء واستخدم قوات الطوارئ في إطلاق النار النهائي للفريق)
translation step: ar => bn
Map(انضم إلى صفوف القوة الأسطورية للجنود والعلماء والمغامرين والغرابة ، والمعروفة باسم [فارغ] -> [ফাঁকা] হিসাবে পরিচিত সৈন্য, বিজ্ঞানী, অ্যাডভেঞ্চারস এবং ওয়েয়ার্ডোসের কিংবদন্তী শক্তির পদে যোগদান করুন, اختر بطلك واشتبك في ساحات القتال في الغد ، وضيع الوقت ، وتجاهل الفيزياء واستخدم قوات الطوارئ في إطلاق النار النهائي للفريق -> আগামীকাল রণক্ষেত্রগুলিতে আপনার নায়ক এবং সংঘর্ষ চয়ন করুন, সময় নষ্ট করুন, পদার্থবিদ্যাকে উপেক্ষা করুন এবং চূড়ান্ত দলের শুটিংয়ে জরুরি বাহিনী ব্যবহার করুন)
translation step: bn => zh-tw
Map([ফাঁকা] হিসাবে পরিচিত সৈন্য, বিজ্ঞানী, অ্যাডভেঞ্চারস এবং ওয়েয়ার্ডোসের কিংবদন্তী শক্তির পদে যোগদান করুন -> 加入被稱為[空白]的士兵，科學家，歷險記和Weirdos傳奇行列, আগামীকাল রণক্ষেত্রগুলিতে আপনার নায়ক এবং সংঘর্ষ চয়ন করুন, সময় নষ্ট করুন, পদার্থবিদ্যাকে উপেক্ষা করুন এবং চূড়ান্ত দলের শুটিংয়ে জরুরি বাহিনী ব্যবহার করুন -> 選擇明天在戰場上的英雄和戰鬥，浪費時間，不理會物理學，並使用緊急部隊射擊最終的團隊)
translation step: zh-tw => cs
Map(加入被稱為[空白]的士兵，科學家，歷險記和Weirdos傳奇行列 -> Přidejte se k řadám vojáků, vědců, dobrodružství a Weirdosových legend zvaných [Blank], 選擇明天在戰場上的英雄和戰鬥，浪費時間，不理會物理學，並使用緊急部隊射擊最終的團隊 -> Vyberte si hrdinu a bojujte zítra na bojišti, ztrácejte čas, ignorujte fyziku a pomocí pohotovostních sil střílejte na finální tým)
translation step: cs => nl
Map(Přidejte se k řadám vojáků, vědců, dobrodružství a Weirdosových legend zvaných [Blank] -> Sluit je aan bij de rijen soldaten, wetenschappers, avonturen en Weirdos-legendes genaamd [Blank], Vyberte si hrdinu a bojujte zítra na bojišti, ztrácejte čas, ignorujte fyziku a pomocí pohotovostních sil střílejte na finální tým -> Kies een held en vecht morgen op het slagveld, verspil tijd, negeer fysica en schiet met de hulpdiensten op het laatste team)
translation step: nl => eo
Map(Sluit je aan bij de rijen soldaten, wetenschappers, avonturen en Weirdos-legendes genaamd [Blank] -> Aliĝu al la vicoj de soldatoj, sciencistoj, aventuroj kaj strangaj legendoj nomataj [Malplena], Kies een held en vecht morgen op het slagveld, verspil tijd, negeer fysica en schiet met de hulpdiensten op het laatste team -> Elektu heroon kaj batalu sur la batalkampo morgaŭ, malŝparu tempon, ignoru fizikon kaj pafu la lastan teamon per la savo)
translation step: eo => fi
Map(Aliĝu al la vicoj de soldatoj, sciencistoj, aventuroj kaj strangaj legendoj nomataj [Malplena] -> Liity sotilaiden, tutkijoiden, seikkailujen ja outojen legendojen joukkoon [Tyhjä], Elektu heroon kaj batalu sur la batalkampo morgaŭ, malŝparu tempon, ignoru fizikon kaj pafu la lastan teamon per la savo -> Valitse sankari ja taistele taistelukentällä huomenna, tuhlaa aikaa, jätä huomiotta fysiikka ja ammu viimeinen joukkue pelastuksella)
translation step: fi => el
Map(Liity sotilaiden, tutkijoiden, seikkailujen ja outojen legendojen joukkoon [Tyhjä] -> Συμμετάσχετε στις τάξεις των στρατιωτών, των επιστημόνων, των τυχοδιώξεων και των περίεργων θρύλων [Άδειο], Valitse sankari ja taistele taistelukentällä huomenna, tuhlaa aikaa, jätä huomiotta fysiikka ja ammu viimeinen joukkue pelastuksella -> Επιλέξτε τον ήρωά σας και πολεμήστε αύριο στο πεδίο της μάχης, χάστε χρόνο, αγνοήστε τη φυσική και πυροβολήστε την τελευταία ομάδα για τη διάσωση)
translation step: el => ht
Map(Συμμετάσχετε στις τάξεις των στρατιωτών, των επιστημόνων, των τυχοδιώξεων και των περίεργων θρύλων [Άδειο] -> Antre nan ranje sòlda, syantis, avanturyé ak ​​lejand etranj [Vide], Επιλέξτε τον ήρωά σας και πολεμήστε αύριο στο πεδίο της μάχης, χάστε χρόνο, αγνοήστε τη φυσική και πυροβολήστε την τελευταία ομάδα για τη διάσωση -> Chwazi ewo ou ak goumen demen sou chan batay la, pèdi tan, inyore fizik ak tire ekip ki sot pase a pote sekou bay la)
translation step: ht => iw
Map(Antre nan ranje sòlda, syantis, avanturyé ak ​​lejand etranj [Vide] -> הצטרף לשורות חיילים, מדענים, הרפתקנים ואגדות מוזרות [ריק], Chwazi ewo ou ak goumen demen sou chan batay la, pèdi tan, inyore fizik ak tire ekip ki sot pase a pote sekou bay la -> בחרו את הגיבור שלכם ולחמו מחר בשדה הקרב, הפסידו זמן, התעלמו מפיזיקה וירו בצוות האחרון להצלה)
translation step: iw => ta
Map(הצטרף לשורות חיילים, מדענים, הרפתקנים ואגדות מוזרות [ריק] -> வீரர்கள், விஞ்ஞானிகள், சாகசக்காரர்கள் மற்றும் விசித்திரமான புராணக்கதைகளில் சேரவும் [வெற்று], בחרו את הגיבור שלכם ולחמו מחר בשדה הקרב, הפסידו זמן, התעלמו מפיזיקה וירו בצוות האחרון להצלה -> உங்கள் ஹீரோவைத் தேர்ந்தெடுத்து நாளை போர்க்களத்தில் சண்டையிடுங்கள், நேரத்தை இழந்துவிடுங்கள், இயற்பியலைப் புறக்கணித்து கடைசி மீட்புக் குழுவைச் சுட்டுவிடுங்கள்)
translation step: ta => uz
Map(வீரர்கள், விஞ்ஞானிகள், சாகசக்காரர்கள் மற்றும் விசித்திரமான புராணக்கதைகளில் சேரவும் [வெற்று] -> Jangchilar, olimlar, sarguzashtlar va g'alati afsonalarga qo'shiling [bo'sh], உங்கள் ஹீரோவைத் தேர்ந்தெடுத்து நாளை போர்க்களத்தில் சண்டையிடுங்கள், நேரத்தை இழந்துவிடுங்கள், இயற்பியலைப் புறக்கணித்து கடைசி மீட்புக் குழுவைச் சுட்டுவிடுங்கள் -> O'zingizning qahramoningizni tanlang va ertaga jang maydonida jang qiling, vaqtni sarflang, fizikani e'tiborsiz qoldiring va oxirgi qutqaruv guruhini otib tashlang)
translation step: uz => vi
Map(Jangchilar, olimlar, sarguzashtlar va g'alati afsonalarga qo'shiling [bo'sh] -> Tham gia Chiến binh, Nhà khoa học, Cuộc phiêu lưu và Huyền thoại kỳ lạ [trống], O'zingizning qahramoningizni tanlang va ertaga jang maydonida jang qiling, vaqtni sarflang, fizikani e'tiborsiz qoldiring va oxirgi qutqaruv guruhini otib tashlang -> Chọn anh hùng của bạn và chiến đấu trên chiến trường vào ngày mai, dành thời gian, bỏ qua vật lý và bắn đội giải cứu cuối cùng)
translation step: vi => cy
Map(Tham gia Chiến binh, Nhà khoa học, Cuộc phiêu lưu và Huyền thoại kỳ lạ [trống] -> Ymunwch â Rhyfelwyr, Gwyddonwyr, Anturiaethau a Chwedlau Rhyfedd [gwag], Chọn anh hùng của bạn và chiến đấu trên chiến trường vào ngày mai, dành thời gian, bỏ qua vật lý và bắn đội giải cứu cuối cùng -> Dewiswch eich arwr ac ymladd ar faes y gad yfory, cymerwch amser, sgipiwch ffiseg a saethwch y tîm achub eithaf)
translation step: cy => xh
Map(Ymunwch â Rhyfelwyr, Gwyddonwyr, Anturiaethau a Chwedlau Rhyfedd [gwag] -> Joyina amaQhawe, iiNzululwazi, iiAdventures kunye neeNdaba eziQhelekileyo, Dewiswch eich arwr ac ymladd ar faes y gad yfory, cymerwch amser, sgipiwch ffiseg a saethwch y tîm achub eithaf -> Khetha iqhawe lakho ulwe kwibala lomlo ngomso, thatha ixesha, tsiba ifiziksi kwaye udubule iqela lokuhlangula lokugqibela)
translation step: xh => yo
Map(Joyina amaQhawe, iiNzululwazi, iiAdventures kunye neeNdaba eziQhelekileyo -> Darapọ mọ Awọn Bayani Agbayani, Awọn Onimo Sayensi, Awọn Irinajo ati Awọn Itan Gbogbogbo, Khetha iqhawe lakho ulwe kwibala lomlo ngomso, thatha ixesha, tsiba ifiziksi kwaye udubule iqela lokuhlangula lokugqibela -> Yan akikanju rẹ ki o ja ni oju-ogun ni ọla, gba akoko rẹ, fo si fisiksi ati titu egbe igbala ti o kẹhin)
translation step: yo => en
Map(Darapọ mọ Awọn Bayani Agbayani, Awọn Onimo Sayensi, Awọn Irinajo ati Awọn Itan Gbogbogbo -> Join the Heroes, Scientists, Tourists and General Stories, Yan akikanju rẹ ki o ja ni oju-ogun ni ọla, gba akoko rẹ, fo si fisiksi ati titu egbe igbala ti o kẹhin -> Choose your hero and fight on the battlefield tomorrow, take your time, jump to physics and shoot the last rescue team)
Map(Join the ranks of the legendary force of soldiers, scientists, adventurers and strangeness, known as [blank] -> Join the Heroes, Scientists, Tourists and General Stories, Choose your hero and clash on the battlefields of tomorrow, waste time, ignore the physics and use emergency forces in the final team shooting -> Choose your hero and fight on the battlefield tomorrow, take your time, jump to physics and shoot the last rescue team)
```

note: Google Translate may ban you for some time if you use it too much.

## running with backend:
the backend requires a running postgres instance, configured with the environment variable `DATABASE_URL`.

ie:
`DATABASE_URL="jdbc:postgresql://localhost:5432/" sbt`


## related work:

* https://github.com/arnaudjuracek/google-translate-chain
* https://github.com/ssut/py-googletrans
* https://stackoverflow.com/questions/65095668/googletrans-api-attributeerror/65109962#65109962
