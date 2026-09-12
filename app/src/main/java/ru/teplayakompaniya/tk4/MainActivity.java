package ru.teplayakompaniya.tk4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

public class MainActivity extends Activity {

    // Palette from approved mockups
    private static final int GREEN = Color.rgb(7,132,79);
    private static final int GREEN_DARK = Color.rgb(8,107,73);
    private static final int ORANGE = Color.rgb(255,122,22);
    private static final int BLUE = Color.rgb(52,120,229);
    private static final int RED = Color.rgb(244,63,78);
    private static final int INK = Color.rgb(15,31,54);
    private static final int MUTED = Color.rgb(108,122,144);
    private static final int LINE = Color.rgb(230,236,243);
    private static final int BG = Color.rgb(248,250,252);
    private static final int WHITE = Color.WHITE;

    private FrameLayout root;
    private LinearLayout content;
    private final Deque<String> history = new ArrayDeque<>();
    private String screen = "main";

    // Demo state. Later replaced by Google Sheets/API without changing screen contracts.
    private final List<ObjectItem> objects = new ArrayList<>();
    private final List<InstallerItem> installers = new ArrayList<>();
    private final List<MoneyTx> txs = new ArrayList<>();
    private long igorBalance = 420_000;
    private long konstantinBalance = 386_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();
        seedDemoData();

        root = new FrameLayout(this);
        root.setBackgroundColor(WHITE);
        setContentView(root);

        showMain(false);
    }

    private void configureSystemBars() {
        Window w = getWindow();
        w.setStatusBarColor(WHITE);
        w.setNavigationBarColor(WHITE);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        } else if (android.os.Build.VERSION.SDK_INT >= 23) {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // We deliberately do NOT enable fullscreen/edge-to-edge.
        // Content stays inside Android's safe system area on phones/tablets.
    }

    private void seedDemoData() {
        objects.add(new ObjectItem("OBJ-001","Мытищи, ул. Лесная, 12","Сидоров А.В.","Комплексное утепление","В работе",65,690000,450000,"Константин","Игорь Игоревич","Алексей Смирнов, Илья Орлов"));
        objects.add(new ObjectItem("OBJ-002","Королёв, ул. Полевая, 7","Петрова Е.С.","Утепление","Запланирован",0,600000,0,"Константин","Игорь Игоревич","Сергей Плотников"));
        objects.add(new ObjectItem("OBJ-003","Пушкино, СНТ Берёзка","Иванов М.С.","Тёплый пол","В работе",25,800000,200000,"Константин","Игорь Игоревич","Алексей Смирнов"));
        objects.add(new ObjectItem("OBJ-004","Ивантеевка, ул. Южная, 3","Кузнецов О.В.","Фасад + утепление","Подтверждён клиентом",0,500000,0,"Константин","Игорь Игоревич","Не назначены"));

        installers.add(new InstallerItem("INS-001","Алексей Смирнов",22,8,4,92000,30000,3,"12.08.2026","На объекте"));
        installers.add(new InstallerItem("INS-002","Илья Орлов",20,10,3,94000,20000,3,"12.08.2026","На объекте"));
        installers.add(new InstallerItem("INS-003","Сергей Плотников",18,12,2,71000,15000,4,"01.09.2026","Выходной"));

        txs.add(new MoneyTx("INCOME","Оплата по договору №125","Мытищи, ул. Лесная, 12",350000));
        txs.add(new MoneyTx("EXPENSE","Покупка материалов","Пушкино, СНТ Берёзка",125000));
        txs.add(new MoneyTx("EXPENSE","Аванс монтажнику","Сергей Плотников",80000));
        txs.add(new MoneyTx("TRANSFER","Константин → Игорь","Внутренний перевод",50000));
    }

    // ---------- navigation ----------

    private void navigate(String target) {
        if (!screen.equals(target)) history.push(screen);
        screen = target;
        render();
    }

    private void render() {
        switch (screen) {
            case "main": showMain(true); break;
            case "objects": showObjects(); break;
            case "create": showCreateObject(); break;
            case "installers": showInstallers(); break;
            case "finance": showFinance(); break;
            case "analytics": showAnalytics(); break;
            case "calendar": showCalendar(); break;
            case "settings": showSettings(); break;
            case "engineers": showEngineers(); break;
            case "managers": showManagers(); break;
            case "surveys": showSurveys(); break;
            default:
                if (screen.startsWith("object:")) showObjectDetail(screen.substring(7));
                else if (screen.startsWith("installer:")) showInstallerDetail(screen.substring(10));
                else if (screen.startsWith("kpi:")) showKpiDetail(screen.substring(4));
                else if (screen.startsWith("tech:")) showTechTask(screen.substring(5));
                else showMain(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!history.isEmpty()) {
            screen = history.pop();
            render();
        } else {
            super.onBackPressed();
        }
    }

    // ---------- frame / reusable UI ----------

    private void beginScreen(boolean showBottomNav) {
        root.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(WHITE);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(10), dp(16), dp(showBottomNav ? 92 : 24));
        scroll.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(scroll, sp);

        if (showBottomNav) addBottomNav();
    }

    private void addBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6), dp(5), dp(6), dp(5));
        nav.setBackgroundColor(WHITE);
        nav.setElevation(dp(14));

        String[][] items = {
                {"⌂","Главная","main"},
                {"☑","План","calendar"},
                {"+","Добавить","create"},
                {"♢","Уведомления","kpi:notifications"},
                {"○","Профиль","settings"}
        };
        for (String[] it : items) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.setPadding(dp(4), dp(2), dp(4), dp(2));
            TextView icon = tv(it[0], it[2].equals("create") ? 28 : 22, it[2].equals(screen) || ("main".equals(it[2]) && "main".equals(screen)) ? GREEN : MUTED, Typeface.BOLD);
            TextView label = tv(it[1], 10, it[2].equals(screen) ? GREEN : MUTED, Typeface.NORMAL);
            box.addView(icon);
            box.addView(label);
            box.setOnClickListener(v -> navigate(it[2]));
            nav.addView(box, new LinearLayout.LayoutParams(0, dp(64), 1));
        }

        FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(74), Gravity.BOTTOM);
        root.addView(nav, np);
    }

    private void appBar(String title, String subtitle) {
        LinearLayout row = h();
        TextView back = pillText("‹", 26, INK, Color.rgb(246,248,251));
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> onBackPressed());
        row.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout titles = v();
        TextView t = tv(title, 22, INK, Typeface.BOLD);
        TextView s = tv(subtitle, 11, MUTED, Typeface.NORMAL);
        titles.addView(t); titles.addView(s);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tp.setMargins(dp(10),0,dp(8),0);
        row.addView(titles,tp);

        TextView menu = pillText("⋯",22,INK,Color.rgb(246,248,251));
        menu.setGravity(Gravity.CENTER);
        row.addView(menu,new LinearLayout.LayoutParams(dp(44),dp(44)));
        content.addView(row, lpMatch(dp(54), 0));
    }

    private void approvedHeader() {
        LinearLayout brandRow = h();
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.company_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        brandRow.addView(logo,new LinearLayout.LayoutParams(dp(46),dp(46)));

        LinearLayout b = v();
        b.setPadding(dp(8),0,0,0);
        b.addView(tv("ТЁПЛАЯ КОМПАНИЯ",17,Color.rgb(4,70,50),Typeface.BOLD));
        b.addView(tv("Строим тепло вместе",11,MUTED,Typeface.NORMAL));
        brandRow.addView(b,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));

        TextView bell = pillText("♢",24,INK,Color.TRANSPARENT);
        bell.setGravity(Gravity.CENTER);
        bell.setOnClickListener(v -> navigate("kpi:notifications"));
        brandRow.addView(bell,new LinearLayout.LayoutParams(dp(44),dp(44)));
        content.addView(brandRow);

        LinearLayout hello = h();
        TextView avatar = pillText("ИИ",16,WHITE,GREEN_DARK);
        avatar.setGravity(Gravity.CENTER);
        hello.addView(avatar,new LinearLayout.LayoutParams(dp(54),dp(54)));
        LinearLayout htxt = v();
        htxt.setPadding(dp(10),0,0,0);
        htxt.addView(tv("Добрый день,",12,MUTED,Typeface.NORMAL));
        htxt.addView(tv("Игорь Игоревич",20,INK,Typeface.BOLD));
        htxt.addView(tv("Руководитель",11,MUTED,Typeface.NORMAL));
        hello.addView(htxt,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        LinearLayout date = v();
        date.setGravity(Gravity.CENTER_VERTICAL);
        date.addView(tv("Сегодня",11,MUTED,Typeface.NORMAL));
        date.addView(tv("12 сентября 2026",13,INK,Typeface.BOLD));
        hello.addView(date);
        LinearLayout.LayoutParams hp=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);
        hp.setMargins(0,dp(8),0,dp(10));
        content.addView(hello,hp);
    }

    private void periodSelector() {
        LinearLayout period = h();
        period.setPadding(dp(4),dp(4),dp(4),dp(4));
        period.setBackground(round(Color.rgb(243,246,249),18));
        String[] labels={"Сегодня","Неделя","Месяц","Квартал","Год"};
        for(String x:labels){
            TextView t=tv(x,11,"Месяц".equals(x)?WHITE:Color.rgb(76,92,116),Typeface.NORMAL);
            t.setGravity(Gravity.CENTER);
            if("Месяц".equals(x)) t.setBackground(round(GREEN,14));
            period.addView(t,new LinearLayout.LayoutParams(0,dp(40),1));
        }
        content.addView(period);
        spacer(10);
    }

    private void sectionTitle(String title, String action, View.OnClickListener actionClick) {
        LinearLayout r=h();
        TextView t=tv(title,18,INK,Typeface.BOLD);
        r.addView(t,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        if(action!=null){
            TextView a=tv(action,11,MUTED,Typeface.NORMAL);
            if(actionClick!=null) a.setOnClickListener(actionClick);
            r.addView(a);
        }
        LinearLayout.LayoutParams rp=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);
        rp.setMargins(0,dp(15),0,dp(7));
        content.addView(r,rp);
    }

    private LinearLayout kpiCard(String icon, String label, String value, String delta, int tint, String target) {
        LinearLayout c=v();
        c.setPadding(dp(12),dp(11),dp(12),dp(10));
        c.setBackground(round(light(tint),18));
        c.setElevation(dp(1));

        LinearLayout top=h();
        TextView ico=pillText(icon,20,WHITE,tint);
        ico.setGravity(Gravity.CENTER);
        top.addView(ico,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout labels=v();
        labels.setPadding(dp(8),0,0,0);
        labels.addView(tv(label,12,INK,Typeface.BOLD));
        labels.addView(tv(value,20,INK,Typeface.BOLD));
        top.addView(labels,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        top.addView(tv("›",23,MUTED,Typeface.NORMAL));
        c.addView(top);
        if(delta!=null&&!delta.isEmpty()) c.addView(tv(delta,11,delta.startsWith("↓")?RED:GREEN,Typeface.BOLD));
        if(target!=null) c.setOnClickListener(vv->navigate(target));
        return c;
    }

    private LinearLayout miniCard(String icon, String label, String value, int tint, String target) {
        LinearLayout c=v();
        c.setPadding(dp(9),dp(8),dp(9),dp(8));
        c.setBackground(round(WHITE,15));
        c.setElevation(dp(1));
        LinearLayout r=h();
        TextView i=tv(icon,19,tint,Typeface.BOLD);
        r.addView(i,new LinearLayout.LayoutParams(dp(28),dp(28)));
        LinearLayout txt=v();
        txt.addView(tv(label,10,MUTED,Typeface.NORMAL));
        txt.addView(tv(value,15,INK,Typeface.BOLD));
        r.addView(txt,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        c.addView(r);
        if(target!=null) c.setOnClickListener(vv->navigate(target));
        return c;
    }

    private LinearLayout quickCard(String icon, String label, int tint, String target) {
        LinearLayout c=v();
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(5),dp(9),dp(5),dp(9));
        c.setBackground(round(WHITE,15));
        c.setElevation(dp(1));
        c.addView(tv(icon,24,tint,Typeface.BOLD));
        c.addView(tv(label,10,INK,Typeface.BOLD));
        if(target!=null)c.setOnClickListener(v->navigate(target));
        return c;
    }

    private LinearLayout attention(String icon,String title,String sub,int tint,String target){
        LinearLayout r=h();
        r.setPadding(dp(9),dp(9),dp(9),dp(9));
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(round(WHITE,14));
        TextView i=pillText(icon,17,WHITE,tint);
        i.setGravity(Gravity.CENTER);
        r.addView(i,new LinearLayout.LayoutParams(dp(34),dp(34)));
        LinearLayout text=v();
        text.setPadding(dp(9),0,dp(4),0);
        text.addView(tv(title,12,INK,Typeface.BOLD));
        text.addView(tv(sub,10,MUTED,Typeface.NORMAL));
        r.addView(text,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        r.addView(tv("›",22,MUTED,Typeface.NORMAL));
        if(target!=null)r.setOnClickListener(v->navigate(target));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);
        p.setMargins(0,0,0,dp(6));
        r.setLayoutParams(p);
        return r;
    }

    // ---------- approved leader main ----------

    private void showMain(boolean fromNav) {
        screen="main";
        if(fromNav) history.clear();
        beginScreen(true);
        approvedHeader();
        periodSelector();

        LinearLayout row1=h(); row1.setWeightSum(2f);
        row1.addView(kpiCard("₽","Оборот","1 245 000 ₽","↑ +12% к прошлому месяцу",GREEN,"kpi:turnover"),weight());
        row1.addView(kpiCard("▥","Прибыль","425 000 ₽","↑ +18% к прошлому месяцу",BLUE,"kpi:profit"),weightMarginLeft());
        content.addView(row1);

        spacer(8);
        LinearLayout row2=h(); row2.setWeightSum(2f);
        row2.addView(kpiCard("🛠","Монтажи","8","↑ +14% к прошлому месяцу",ORANGE,"kpi:installations"),weight());
        row2.addView(kpiCard("⌂","Объекты в работе","11","↑ +22% к прошлому месяцу",GREEN,"objects"),weightMarginLeft());
        content.addView(row2);

        sectionTitle("Ключевые показатели","Этот месяц⌄",null);
        addThreeMini(miniCard("●●","Лиды","42",BLUE,"kpi:leads"),
                miniCard("▰","Замеры","20",ORANGE,"surveys"),
                miniCard("▣","Договоры","11",GREEN,"kpi:contracts"));
        spacer(7);
        addThreeMini(miniCard("≋","Средний чек","113 000 ₽",ORANGE,"kpi:avg"),
                miniCard("◷","Дебиторка","320 000 ₽",RED,"kpi:debt"),
                miniCard("↓","Расходы","820 000 ₽",RED,"finance"));

        sectionTitle("Быстрый доступ",null,null);
        addFourQuick(
                quickCard("⌂","Объекты",GREEN,"objects"),
                quickCard("●●","Монтажники",ORANGE,"installers"),
                quickCard("♟","Инженеры",BLUE,"engineers"),
                quickCard("●●","Менеджеры",RED,"managers")
        );
        spacer(7);
        addFourQuick(
                quickCard("₽","Финансы",GREEN,"finance"),
                quickCard("▥","Аналитика",BLUE,"analytics"),
                quickCard("▦","Календарь",RED,"calendar"),
                quickCard("⚙","Справочники",MUTED,"settings")
        );

        sectionTitle("Сегодня требует внимания","Все уведомления ›",v->navigate("kpi:notifications"));
        content.addView(attention("!","Просрочена задача","Объект ул. Лесная, 12 — не выполнена проверка",RED,"object:OBJ-001"));
        content.addView(attention("▥","Низкая конверсия","Замеры → Договоры: 20% (норма от 35%)",ORANGE,"surveys"));
        content.addView(attention("₽","Есть неоплаченные счета","Дебиторская задолженность: 320 000 ₽",RED,"analytics"));
    }

    // ---------- objects ----------

    private void showObjects() {
        beginScreen(true);
        appBar("Объекты","Все объекты компании");
        sectionTitle("Объекты компании",null,null);

        LinearLayout a=h();
        a.addView(miniCard("⌂","В работе","6",GREEN,"kpi:objects_work"),weight());
        a.addView(miniCard("▦","Запланированы","18",BLUE,"kpi:objects_planned"),weightMarginLeft());
        content.addView(a);
        spacer(7);
        LinearLayout b=h();
        b.addView(miniCard("✓","Подтверждены","7",ORANGE,"kpi:objects_confirmed"),weight());
        b.addView(miniCard("▰","Замеры за месяц","42",BLUE,"surveys"),weightMarginLeft());
        content.addView(b);
        spacer(7);
        content.addView(miniCard("↪","В монтаж перешли","11",BLUE,"surveys"));

        spacer(10);
        LinearLayout filters=h();
        String[] fs={"Все","В работе","Запланированы","Подтверждены","Завершены"};
        for(String f:fs){
            TextView chip=pillText(f,10,"Все".equals(f)?WHITE:INK,"Все".equals(f)?GREEN:Color.rgb(246,248,251));
            chip.setPadding(dp(11),dp(7),dp(11),dp(7));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,dp(34));
            cp.setMargins(0,0,dp(5),0);
            filters.addView(chip,cp);
        }
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);hsv.addView(filters);
        content.addView(hsv);

        LinearLayout search=h();
        EditText e=edit("Поиск по адресу, клиенту…");
        search.addView(e,new LinearLayout.LayoutParams(0,dp(46),1));
        TextView plus=pillText("+",28,WHITE,GREEN);plus.setGravity(Gravity.CENTER);plus.setOnClickListener(v->navigate("create"));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(46),dp(46));pp.setMargins(dp(7),0,0,0);search.addView(plus,pp);
        LinearLayout.LayoutParams sr=lpMatch(dp(50),0);sr.setMargins(0,dp(10),0,dp(4));content.addView(search,sr);

        for(ObjectItem o:objects) content.addView(objectCard(o));
    }

    private View objectCard(ObjectItem o) {
        LinearLayout card=h();
        card.setPadding(dp(10),dp(10),dp(10),dp(10));
        card.setBackground(round(WHITE,16));
        card.setElevation(dp(1));

        TextView photo=pillText("⌂",26,GREEN,Color.rgb(235,247,241));
        photo.setGravity(Gravity.CENTER);
        card.addView(photo,new LinearLayout.LayoutParams(dp(64),dp(64)));

        LinearLayout mid=v(); mid.setPadding(dp(10),0,0,0);
        mid.addView(tv(o.address,13,INK,Typeface.BOLD));
        mid.addView(tv("Клиент: "+o.client,10,MUTED,Typeface.NORMAL));
        mid.addView(tv(o.workType,10,MUTED,Typeface.NORMAL));
        mid.addView(tv("Исполнители: "+o.installers,9,MUTED,Typeface.NORMAL));
        card.addView(mid,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));

        LinearLayout right=v();right.setGravity(Gravity.RIGHT);
        right.addView(statusBadge(o.status));
        if(o.progress>0) right.addView(tv(o.progress+"%",11,GREEN,Typeface.BOLD));
        right.addView(tv("›",22,MUTED,Typeface.NORMAL));
        card.addView(right);

        card.setOnClickListener(v->navigate("object:"+o.id));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,dp(5),0,dp(4));card.setLayoutParams(p);
        return card;
    }

    private void showObjectDetail(String id) {
        ObjectItem o=findObject(id);
        if(o==null){navigate("objects");return;}
        beginScreen(true);
        appBar("Карточка объекта",o.address);

        content.addView(infoHero(o.address,o.status,o.progress));

        sectionTitle("Клиент и объект",null,null);
        content.addView(infoRow("Клиент",o.client));
        content.addView(infoRow("Телефон","+7 915 123-45-67"));
        content.addView(infoRow("Вид работ",o.workType));
        content.addView(infoRow("Инженер",o.engineer));
        content.addView(infoRow("Менеджер",o.manager));
        content.addView(infoRow("Монтажники",o.installers));

        sectionTitle("Финансы объекта",null,null);
        LinearLayout f=h();
        f.addView(miniCard("₽","Договор",money(o.contract),GREEN,"kpi:contract_object"),weight());
        f.addView(miniCard("↓","Получено",money(o.paid),BLUE,"kpi:received_object"),weightMarginLeft());
        content.addView(f);
        spacer(7);
        content.addView(miniCard("◷","Осталось получить",money(o.contract-o.paid),ORANGE,"analytics"));

        sectionTitle("Техническое задание",null,null);
        LinearLayout task=attention("▤","ТЗ готово к формированию","План по дням · монтажники · зарплата · график оплат",ORANGE,"tech:"+o.id);
        content.addView(task);
        content.addView(attention("▦","План и фотоотчёты","Дни работ, факт объёмов, фото ДО/процесс/ПОСЛЕ",BLUE,"tech:"+o.id));
    }

    private void showCreateObject() {
        beginScreen(true);
        appBar("Создать объект","Новый объект в системе");

        LinearLayout sync=v();
        sync.setPadding(dp(12),dp(10),dp(12),dp(10));
        sync.setBackground(round(Color.rgb(238,251,244),16));
        sync.addView(tv("↻ Синхронизация с таблицей",13,GREEN_DARK,Typeface.BOLD));
        sync.addView(tv("Alpha 5: UI и логика готовы. Подключение реальной Google Sheets — следующим этапом.",10,MUTED,Typeface.NORMAL));
        content.addView(sync);

        final EditText client=field("Клиент *","Иванов Сергей Петрович");
        final EditText phone=field("Телефон","+7 915 123-45-67");
        final EditText address=field("Адрес объекта *","Московская обл., г. Химки, ул. Лесная, д. 12");
        final EditText type=field("Вид работ *","Комплексное утепление");
        final EditText sum=field("Сумма договора","350000");
        final EditText survey=field("Дата замера","10.09.2026");
        final EditText plan=field("Плановая дата монтажа","25.09.2026");
        final EditText status=field("Статус объекта","Подтверждён клиентом");
        final EditText engineer=field("Инженер","Константин");

        sectionTitle("Техническое задание",null,null);
        content.addView(attention("▤","ТЗ не создано","После создания объекта можно назначить монтажников и план работ.",RED,null));

        Button tech=primaryOutline("Сделать техническое задание");
        tech.setOnClickListener(v->navigate("tech:NEW"));
        content.addView(tech);

        spacer(8);
        Button save=primary("Сохранить объект");
        save.setOnClickListener(v->{
            long contract=parseLong(sum.getText().toString());
            String id="OBJ-"+String.format(Locale.US,"%03d",objects.size()+1);
            objects.add(0,new ObjectItem(id,address.getText().toString(),client.getText().toString(),type.getText().toString(),status.getText().toString(),0,contract,0,engineer.getText().toString(),"Игорь Игоревич","Не назначены"));
            Toast.makeText(this,"Объект добавлен в локальную Alpha 5",Toast.LENGTH_SHORT).show();
            navigate("objects");
        });
        content.addView(save);
    }

    // ---------- tech task ----------

    private void showTechTask(String objectId) {
        beginScreen(true);
        appBar("Техническое задание","Инженер · объект "+objectId);

        sectionTitle("Монтажники на объекте","Выбор конкретных людей",null);
        for(InstallerItem i:installers){
            LinearLayout r=h();
            r.setPadding(dp(9),dp(8),dp(9),dp(8));
            r.setBackground(round(WHITE,14));
            CheckBox cb=new CheckBox(this);cb.setChecked(!i.name.startsWith("Сергей"));
            r.addView(cb,new LinearLayout.LayoutParams(dp(44),dp(44)));
            LinearLayout name=v();name.addView(tv(i.name,12,INK,Typeface.BOLD));name.addView(tv("Монтажник",9,MUTED,Typeface.NORMAL));
            r.addView(name,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
            EditText amount=edit("Сумма");
            amount.setInputType(InputType.TYPE_CLASS_NUMBER);
            amount.setText(i.name.startsWith("Алексей")?"45000":i.name.startsWith("Илья")?"42000":"40000");
            r.addView(amount,new LinearLayout.LayoutParams(dp(100),dp(44)));
            LinearLayout.LayoutParams rp=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);rp.setMargins(0,0,0,dp(6));content.addView(r,rp);
        }

        sectionTitle("План объекта по дням",null,null);
        content.addView(dayCard("День 1 · 25.09.2026","Монтаж каркаса","100 м²","Ветрозащитная мембрана","100 м²"));
        content.addView(dayCard("День 2 · 26.09.2026","Задувной утеплитель","120 м²","Контроль качества","1 этап"));
        content.addView(dayCard("День 3 · 27.09.2026","Завершение узлов","40 м²","Фото ПОСЛЕ и сдача","обязательно"));

        sectionTitle("График оплат клиента",null,null);
        content.addView(infoRow("25.09.2026","300 000 ₽ · Аванс"));
        content.addView(infoRow("27.09.2026","200 000 ₽ · Этап 2"));
        content.addView(infoRow("30.09.2026","180 000 ₽ · Финальный платёж"));

        Button save=primary("Сохранить ТЗ");
        save.setOnClickListener(v->Toast.makeText(this,"ТЗ сохранено локально",Toast.LENGTH_SHORT).show());
        content.addView(save);
        spacer(7);

        Button share=primaryOutline("Поделиться ТЗ");
        share.setOnClickListener(v->shareTechTask(objectId));
        content.addView(share);
    }

    private View dayCard(String title,String t1,String q1,String t2,String q2){
        LinearLayout c=v();c.setPadding(dp(11),dp(10),dp(11),dp(10));c.setBackground(round(Color.rgb(249,251,253),15));
        c.addView(tv(title,13,INK,Typeface.BOLD));
        c.addView(infoRow(t1,q1));
        c.addView(infoRow(t2,q2));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,0,0,dp(7));c.setLayoutParams(p);
        return c;
    }

    private void shareTechTask(String objectId){
        String text="ТЁПЛАЯ КОМПАНИЯ — ТЕХНИЧЕСКОЕ ЗАДАНИЕ\n"+
                "Объект: "+objectId+"\n"+
                "День 1: Каркас 100 м²; Мембрана 100 м²\n"+
                "День 2: Задувной утеплитель 120 м²\n"+
                "Фото: ДО / Процесс / ПОСЛЕ\n"+
                "Инженер: Константин\n\n"+
                "Финансовые показатели компании в ТЗ монтажников не передаются.";
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT,"ТЗ — "+objectId);
        send.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(Intent.createChooser(send,"Поделиться ТЗ"));
    }

    // ---------- installers ----------

    private void showInstallers() {
        beginScreen(true);
        appBar("Монтажники","Аналитика, выплаты, инструмент");
        periodSelector();

        LinearLayout r1=h();
        r1.addView(kpiCard("●●","Монтажников","8","↑ +14%",ORANGE,"kpi:installers_count"),weight());
        r1.addView(kpiCard("⌂","На объектах сегодня","6","↑ +20%",GREEN,"calendar"),weightMarginLeft());
        content.addView(r1);
        spacer(8);
        LinearLayout r2=h();
        r2.addView(kpiCard("✓","Закрыто объектов","14","↑ +27%",BLUE,"kpi:closed_by_installers"),weight());
        r2.addView(kpiCard("₽","К выдаче","344 000 ₽","↑ +12%",ORANGE,"kpi:payroll"),weightMarginLeft());
        content.addView(r2);

        sectionTitle("Начисления и выплаты",null,null);
        addThreeMini(
                miniCard("≋","Начислено","524 000 ₽",GREEN,"kpi:accrued"),
                miniCard("▭","Выдано / аванс","180 000 ₽",ORANGE,"kpi:paid_installers"),
                miniCard("₽","Осталось выдать","344 000 ₽",RED,"kpi:payroll")
        );

        LinearLayout sync=v();sync.setPadding(dp(10),dp(8),dp(10),dp(8));sync.setBackground(round(Color.rgb(238,251,244),14));
        sync.addView(tv("↻ Синхронизация подготовлена",12,GREEN,Typeface.BOLD));
        sync.addView(tv("В Alpha 5 данные локальные. Google Sheets подключим без изменения интерфейса.",9,MUTED,Typeface.NORMAL));
        content.addView(sync);

        sectionTitle("Сотрудники","Вся аналитика ›",null);
        for(InstallerItem i:installers) content.addView(installerCard(i));
    }

    private View installerCard(InstallerItem i){
        LinearLayout c=v();c.setPadding(dp(10),dp(9),dp(10),dp(9));c.setBackground(round(WHITE,15));c.setElevation(dp(1));
        LinearLayout top=h();
        TextView ava=pillText(initials(i.name),14,WHITE,GREEN_DARK);ava.setGravity(Gravity.CENTER);top.addView(ava,new LinearLayout.LayoutParams(dp(44),dp(44)));
        LinearLayout name=v();name.setPadding(dp(9),0,0,0);name.addView(tv(i.name,12,INK,Typeface.BOLD));name.addView(tv("Монтажник · "+i.status,9,i.status.equals("Выходной")?ORANGE:GREEN,Typeface.NORMAL));
        top.addView(name,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        top.addView(tv("›",22,MUTED,Typeface.NORMAL));
        c.addView(top);
        LinearLayout m=h();
        m.addView(metricTiny(i.workDays+"","раб. дней"),weight());
        m.addView(metricTiny(i.daysOff+"","выходных"),weight());
        m.addView(metricTiny(i.closedObjects+"","объекта"),weight());
        c.addView(m);
        LinearLayout money=h();
        money.addView(metricTiny(money(i.accrued),"начислено"),weight());
        money.addView(metricTiny(money(i.paid),"аванс/выплаты"),weight());
        money.addView(metricTiny(money(i.accrued-i.paid),"к выдаче"),weight());
        c.addView(money);
        c.addView(tv("Инструмент: "+i.tools+" ед. · Форма выдана: "+i.uniformDate,9,MUTED,Typeface.NORMAL));
        c.setOnClickListener(v->navigate("installer:"+i.id));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,0,0,dp(7));c.setLayoutParams(p);
        return c;
    }

    private void showInstallerDetail(String id){
        InstallerItem i=findInstaller(id);if(i==null){navigate("installers");return;}
        beginScreen(true);appBar(i.name,"Монтажник");
        content.addView(infoHero(i.name,i.status,0));
        sectionTitle("Месяц",null,null);
        content.addView(infoRow("Рабочие дни",i.workDays+""));
        content.addView(infoRow("Выходные",i.daysOff+""));
        content.addView(infoRow("Закрыто объектов",i.closedObjects+""));
        content.addView(infoRow("Начислено",money(i.accrued)));
        content.addView(infoRow("Выдано / аванс",money(i.paid)));
        content.addView(infoRow("Осталось выдать",money(i.accrued-i.paid)));
        sectionTitle("Имущество",null,null);
        content.addView(infoRow("Инструмент",i.tools+" единицы · закреплено"));
        content.addView(infoRow("Форма / СИЗ","Выдана "+i.uniformDate));
    }

    // ---------- finance ----------

    private void showFinance() {
        beginScreen(true);appBar("Финансы","Движение денег и контроль расходов");periodSelector();

        long turnover=0,expenses=0;
        for(MoneyTx t:txs){if(t.type.equals("INCOME"))turnover+=t.amount;if(t.type.equals("EXPENSE"))expenses+=t.amount;}
        long totalBalance=igorBalance+konstantinBalance;

        LinearLayout a=h();
        a.addView(kpiCard("₽","Оборот",money(turnover),"Фактические приходы",GREEN,"kpi:turnover"),weight());
        a.addView(kpiCard("↓","Расходы",money(expenses),"По операциям",RED,"kpi:expenses"),weightMarginLeft());
        content.addView(a);spacer(8);
        LinearLayout b=h();
        b.addView(kpiCard("◉","Общий остаток",money(totalBalance),"Живой остаток",BLUE,"kpi:balance"),weight());
        b.addView(kpiCard("▣","Подотчёт",money(totalBalance),"По ответственным",ORANGE,"kpi:accountable"),weightMarginLeft());
        content.addView(b);

        sectionTitle("Остатки у подотчётных лиц",null,null);
        content.addView(infoRow("Игорь",money(igorBalance)));
        content.addView(infoRow("Константин",money(konstantinBalance)));

        LinearLayout acts=h();
        Button in=smallButton("+ Приход",GREEN);in.setOnClickListener(v->moneyDialog("INCOME"));
        Button out=smallButton("− Расход",ORANGE);out.setOnClickListener(v->moneyDialog("EXPENSE"));
        Button move=smallButton("↔ Передать",BLUE);move.setOnClickListener(v->transferDialog());
        acts.addView(in,weight());acts.addView(out,weightMarginLeft());acts.addView(move,weightMarginLeft());
        LinearLayout.LayoutParams ap=lpMatch(dp(48),0);ap.setMargins(0,dp(10),0,dp(5));content.addView(acts,ap);

        sectionTitle("Последние операции","Все операции ›",null);
        for(MoneyTx t:txs){
            int tint=t.type.equals("INCOME")?GREEN:t.type.equals("EXPENSE")?RED:BLUE;
            String sign=t.type.equals("INCOME")?"+":t.type.equals("EXPENSE")?"−":"↔ ";
            content.addView(attention(sign,t.title,t.sub+" · "+money(t.amount),tint,null));
        }

        sectionTitle("Требует внимания",null,null);
        content.addView(attention("!","Приход без получателя","Нужно указать подотчётное лицо",RED,"kpi:notifications"));
        content.addView(attention("!","Расход без объекта","Проверьте назначение расхода",ORANGE,"kpi:notifications"));
    }

    private void moneyDialog(String type){
        EditText amt=new EditText(this);amt.setHint("Сумма");amt.setInputType(InputType.TYPE_CLASS_NUMBER);amt.setPadding(dp(12),dp(10),dp(12),dp(10));
        String title=type.equals("INCOME")?"Добавить приход":"Добавить расход";
        new AlertDialog.Builder(this).setTitle(title).setView(amt)
                .setPositiveButton("Сохранить",(d,w)->{
                    long v=parseLong(amt.getText().toString());
                    if(v<=0)return;
                    if(type.equals("INCOME")){
                        txs.add(0,new MoneyTx("INCOME","Новый приход","Без привязки",v));
                        igorBalance+=v;
                    }else{
                        txs.add(0,new MoneyTx("EXPENSE","Новый расход","Без привязки",v));
                        igorBalance-=v;
                    }
                    showFinance();
                }).setNegativeButton("Отмена",null).show();
    }

    private void transferDialog(){
        EditText amt=new EditText(this);amt.setHint("Сумма Константин → Игорь");amt.setInputType(InputType.TYPE_CLASS_NUMBER);amt.setPadding(dp(12),dp(10),dp(12),dp(10));
        new AlertDialog.Builder(this).setTitle("Передать деньги").setMessage("Внутренний перевод не является расходом и не меняет общий баланс.")
                .setView(amt).setPositiveButton("Передать",(d,w)->{
                    long v=parseLong(amt.getText().toString());
                    if(v>0 && v<=konstantinBalance){
                        konstantinBalance-=v;igorBalance+=v;
                        txs.add(0,new MoneyTx("TRANSFER","Константин → Игорь","Внутренний перевод",v));
                    }
                    showFinance();
                }).setNegativeButton("Отмена",null).show();
    }

    // ---------- analytics ----------

    private void showAnalytics() {
        beginScreen(true);appBar("Аналитика","Объекты, поступления, дебиторка");periodSelector();

        LinearLayout a=h();
        a.addView(kpiCard("₽","Оборот","2 480 000 ₽","Фактические приходы",GREEN,"kpi:turnover"),weight());
        a.addView(kpiCard("◷","Дебиторка","680 000 ₽","Только просрочено",RED,"kpi:debt"),weightMarginLeft());
        content.addView(a);spacer(8);
        LinearLayout b=h();
        b.addView(kpiCard("▥","План поступлений","1 120 000 ₽","Будущие этапы ТЗ",BLUE,"kpi:plan_income"),weight());
        b.addView(kpiCard("◔","Осталось получить","1 800 000 ₽","По договорам",ORANGE,"kpi:remaining"),weightMarginLeft());
        content.addView(b);

        sectionTitle("Фильтры","Этот месяц⌄",null);
        LinearLayout filters=h();
        for(String f:new String[]{"Все объекты","В работе","Запланированы","Завершены","По инженеру","По менеджеру"}){
            TextView chip=pillText(f,9,f.equals("Все объекты")?WHITE:INK,f.equals("Все объекты")?GREEN:Color.rgb(246,248,251));
            chip.setPadding(dp(10),dp(7),dp(10),dp(7));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,dp(34));p.setMargins(0,0,dp(5),0);filters.addView(chip,p);
        }
        HorizontalScrollView hv=new HorizontalScrollView(this);hv.setHorizontalScrollBarEnabled(false);hv.addView(filters);content.addView(hv);

        sectionTitle("Аналитика по объектам","3 объекта ›",null);
        content.addView(analyticsObject("Химки, ул. Лесная, 12","В работе",1200000,800000,400000,0,67,"200 000 ₽ — 25.09.2026"));
        content.addView(analyticsObject("Мытищи, ул. Центральная, 8","Запланирован",950000,0,950000,0,0,"180 000 ₽ — 12.09.2026"));
        content.addView(analyticsObject("Королёв, ул. Полевая, 7","Завершён",600000,400000,0,200000,100,"Просрочка 4 дня"));

        sectionTitle("Требует внимания",null,null);
        content.addView(attention("!","Просрочен платёж — 200 000 ₽","Королёв, ул. Полевая, 7",RED,"kpi:debt"));
        content.addView(attention("◷","Через 2 дня ожидается 180 000 ₽","Мытищи, ул. Центральная, 8",ORANGE,"kpi:plan_income"));
    }

    private View analyticsObject(String addr,String status,long contract,long paid,long plan,long debt,int progress,String next){
        LinearLayout c=v();c.setPadding(dp(10),dp(9),dp(10),dp(9));c.setBackground(round(WHITE,15));c.setElevation(dp(1));
        LinearLayout title=h();title.addView(tv(addr,12,INK,Typeface.BOLD),new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));title.addView(statusBadge(status));c.addView(title);
        LinearLayout m1=h();m1.addView(metricTiny(money(contract),"договор"),weight());m1.addView(metricTiny(money(paid),"получено"),weight());c.addView(m1);
        LinearLayout m2=h();m2.addView(metricTiny(money(plan),"план поступлений"),weight());m2.addView(metricTiny(money(debt),"дебиторка"),weight());c.addView(m2);
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(100);pb.setProgress(progress);pb.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        c.addView(pb,lpMatch(dp(6),0));c.addView(tv(progress+"% готовности · Следующий платёж: "+next,9,debt>0?RED:MUTED,Typeface.NORMAL));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,0,0,dp(7));c.setLayoutParams(p);return c;
    }

    // ---------- surveys / engineers / managers ----------

    private void showSurveys(){
        beginScreen(true);appBar("Замеры","Статистика и движение");
        periodSelector();
        LinearLayout a=h();
        a.addView(kpiCard("▰","Замеров за месяц","42","Общий счётчик",ORANGE,"kpi:surveys_all"),weight());
        a.addView(kpiCard("↪","Из замеров в монтаж","11","Конверсия 26%",GREEN,"kpi:surveys_to_install"),weightMarginLeft());
        content.addView(a);
        sectionTitle("Контроль базы замеров",null,null);
        content.addView(attention("!","Без движения более 14 дней","7 замеров требуют решения",ORANGE,"kpi:survey_14"));
        content.addView(attention("◷","Без повторного касания более 21 дня","4 замера требуют прозвона",RED,"kpi:survey_21"));
        content.addView(infoRow("Поиск замера","По последним 4 цифрам телефона"));
        content.addView(infoRow("История","Замер сохраняется после перевода в объект"));
    }

    private void showEngineers(){
        beginScreen(true);appBar("Инженеры","Объекты, ТЗ, загрузка и отклонения");
        content.addView(kpiCard("♟","Константин","6 активных объектов","2 ТЗ требуют внимания",BLUE,"kpi:engineer_const"));
        sectionTitle("Функции инженера",null,null);
        content.addView(infoRow("Объекты","Подтверждённые / запланированные / в работе"));
        content.addView(infoRow("Замеры","Поиск по последним 4 цифрам телефона"));
        content.addView(infoRow("Техническое задание","Монтажники · зарплата · дни · объёмы · график оплат"));
        content.addView(infoRow("Контроль дня","План/факт · фото · расходы · переносы"));
        content.addView(attention("!","2 объекта без готового ТЗ","Открыть список",RED,"objects"));
    }

    private void showManagers(){
        beginScreen(true);appBar("Менеджеры","Лиды, замеры, договоры, конверсия");
        content.addView(kpiCard("●●","Лиды","42","Источники и CPL",BLUE,"kpi:leads"));
        content.addView(kpiCard("▰","Замеры","20","Назначено / проведено",ORANGE,"surveys"));
        content.addView(kpiCard("▣","Договоры","11","Переход к объектам",GREEN,"objects"));
        sectionTitle("Принцип доступа",null,null);
        content.addView(infoRow("Менеджер видит","Клиенты, заявки, замеры, КП, статусы продаж"));
        content.addView(infoRow("Менеджер не видит","Прибыль, общую кассу, подотчёт и чужие зарплаты"));
    }

    // ---------- calendar / settings ----------

    private void showCalendar(){
        beginScreen(true);appBar("Календарь","План работ и загрузка монтажников");
        sectionTitle("Сентябрь 2026","День · Неделя · Месяц",null);
        LinearLayout days=h();
        for(String d:new String[]{"8","9","10","11","12","13","14"}){
            TextView t=pillText(d,12,d.equals("12")?WHITE:INK,d.equals("12")?GREEN:Color.rgb(246,248,251));t.setGravity(Gravity.CENTER);
            days.addView(t,new LinearLayout.LayoutParams(0,dp(42),1));
        }
        content.addView(days);
        sectionTitle("Загрузка по людям",null,null);
        content.addView(scheduleRow("Алексей Смирнов","Мытищи, ул. Лесная, 12","В работе",GREEN));
        content.addView(scheduleRow("Илья Орлов","Мытищи, ул. Лесная, 12","В работе",GREEN));
        content.addView(scheduleRow("Сергей Плотников","Выходной","Свободен",ORANGE));
        content.addView(scheduleRow("Андрей Крылов","Королёв, ул. Полевая, 7","Запланирован",BLUE));
        content.addView(attention("!","Контроль конфликта","При пересечении дат одного монтажника система должна блокировать двойное назначение.",RED,"kpi:calendar_conflict"));
    }

    private void showSettings(){
        beginScreen(true);appBar("Настройки","Профиль, справочники, приложение");
        LinearLayout profile=h();
        TextView a=pillText("ИИ",16,WHITE,GREEN_DARK);a.setGravity(Gravity.CENTER);profile.addView(a,new LinearLayout.LayoutParams(dp(54),dp(54)));
        LinearLayout p=v();p.setPadding(dp(10),0,0,0);p.addView(tv("Игорь Игоревич",15,INK,Typeface.BOLD));p.addView(tv("Руководитель · ADMIN",11,MUTED,Typeface.NORMAL));profile.addView(p,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        content.addView(profile);
        sectionTitle("Интерфейс",null,null);
        content.addView(infoRow("Фото профиля","Камера / Галерея — будет подключено"));
        content.addView(infoRow("Тема","Светлая · Тёмная · Как на устройстве"));
        content.addView(infoRow("Уведомления","Объекты · ТЗ · платежи · отчёты"));
        sectionTitle("Система",null,null);
        content.addView(infoRow("Синхронизация","Google Sheets/API — следующий этап"));
        content.addView(infoRow("Справочники","Виды работ · статьи · статусы · причины · единицы"));
        content.addView(infoRow("Пользователи и роли","Роль назначает только руководитель/администратор"));
        content.addView(infoRow("Версия","5.0.0-alpha5 · Native Clean"));
        content.addView(infoRow("Безопасная зона","Системные панели Android не перекрывают контент"));
    }

    // ---------- KPI drilldown ----------

    private void showKpiDetail(String key){
        beginScreen(true);
        String title=humanKpi(key);
        appBar(title,"Детализация показателя");
        content.addView(kpiCard("▥",title,kpiValue(key),"Нажмите на строку ниже для первичной записи",GREEN,null));
        sectionTitle("Расшифровка",null,null);

        switch(key){
            case "turnover":
                content.addView(infoRow("Мытищи, ул. Лесная, 12","+350 000 ₽"));
                content.addView(infoRow("Пушкино, СНТ Берёзка","+420 000 ₽"));
                content.addView(infoRow("Другие фактические приходы","+475 000 ₽"));
                break;
            case "profit":
                content.addView(infoRow("Фактическая прибыль","Только объекты Закрыт 100%"));
                content.addView(infoRow("Планируемая прибыль","Договор − планируемые расходы незакрытых объектов"));
                break;
            case "debt":
                content.addView(attention("!","Королёв, ул. Полевая, 7","Просрочено 200 000 ₽ · 4 дня",RED,"object:OBJ-002"));
                content.addView(infoRow("Правило","Не весь остаток договора. Только просроченные этапы ТЗ."));
                break;
            case "leads":
                for(String s:new String[]{"Яндекс Директ — 18","Авито — 9","Telegram — 5","VK — 4","YouTube — 3","Рекомендации — 3"}) content.addView(infoRow("Источник",s));
                break;
            case "notifications":
                content.addView(attention("!","Просрочена задача","ул. Лесная, 12",RED,"object:OBJ-001"));
                content.addView(attention("◷","4 замера без касания","Более 21 дня",ORANGE,"surveys"));
                break;
            default:
                content.addView(infoRow("Показатель",title));
                content.addView(infoRow("Статус","Экран детализации активен и готов к подключению реальных записей API."));
        }
    }

    // ---------- helpers ----------

    private String humanKpi(String k){
        Map<String,String> m=new HashMap<>();
        m.put("turnover","Оборот");m.put("profit","Прибыль");m.put("installations","Монтажи");m.put("leads","Лиды");
        m.put("contracts","Договоры");m.put("avg","Средний чек");m.put("debt","Дебиторка");m.put("expenses","Расходы");
        m.put("plan_income","План поступлений");m.put("remaining","Осталось получить");m.put("balance","Общий остаток");
        m.put("accountable","Подотчёт");m.put("notifications","Уведомления");
        return m.getOrDefault(k,k.replace('_',' '));
    }
    private String kpiValue(String k){
        if(k.equals("turnover"))return "1 245 000 ₽";if(k.equals("profit"))return "425 000 ₽";if(k.equals("debt"))return "320 000 ₽";
        if(k.equals("leads"))return "42";if(k.equals("contracts"))return "11";if(k.equals("avg"))return "113 000 ₽";return "Подробнее";
    }

    private ObjectItem findObject(String id){for(ObjectItem o:objects)if(o.id.equals(id))return o;return null;}
    private InstallerItem findInstaller(String id){for(InstallerItem i:installers)if(i.id.equals(id))return i;return null;}

    private LinearLayout infoHero(String title,String status,int progress){
        LinearLayout c=v();c.setPadding(dp(14),dp(13),dp(14),dp(13));c.setBackground(round(Color.rgb(239,251,247),18));
        c.addView(tv(title,18,INK,Typeface.BOLD));c.addView(statusBadge(status));
        if(progress>0){
            ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(100);pb.setProgress(progress);pb.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
            c.addView(pb,lpMatch(dp(7),0));c.addView(tv("Готовность "+progress+"%",10,GREEN,Typeface.BOLD));
        }
        return c;
    }

    private View infoRow(String left,String right){
        LinearLayout r=h();r.setPadding(dp(10),dp(9),dp(10),dp(9));r.setGravity(Gravity.CENTER_VERTICAL);r.setBackground(round(WHITE,13));
        r.addView(tv(left,11,MUTED,Typeface.NORMAL),new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        TextView v=tv(right,11,INK,Typeface.BOLD);v.setGravity(Gravity.RIGHT);r.addView(v,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,0,0,dp(5));r.setLayoutParams(p);return r;
    }

    private View metricTiny(String value,String label){
        LinearLayout b=v();b.setPadding(dp(4),dp(5),dp(4),dp(5));b.addView(tv(value,11,INK,Typeface.BOLD));b.addView(tv(label,8,MUTED,Typeface.NORMAL));return b;
    }

    private View scheduleRow(String name,String object,String status,int tint){
        LinearLayout r=h();r.setPadding(dp(9),dp(9),dp(9),dp(9));r.setBackground(round(WHITE,14));
        TextView a=pillText(initials(name),12,WHITE,tint);a.setGravity(Gravity.CENTER);r.addView(a,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout x=v();x.setPadding(dp(9),0,0,0);x.addView(tv(name,11,INK,Typeface.BOLD));x.addView(tv(object,9,MUTED,Typeface.NORMAL));r.addView(x,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));r.addView(statusBadge(status));
        LinearLayout.LayoutParams p=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);p.setMargins(0,0,0,dp(6));r.setLayoutParams(p);return r;
    }

    private TextView statusBadge(String status){
        int color=status.contains("работ")?GREEN:status.contains("Заплан")?BLUE:status.contains("Подтверж")?ORANGE:status.contains("Выход")?ORANGE:GREEN;
        TextView b=pillText(status,9,color,light(color));b.setPadding(dp(8),dp(5),dp(8),dp(5));return b;
    }

    private EditText field(String label,String value){
        TextView l=tv(label,10,MUTED,Typeface.BOLD);LinearLayout.LayoutParams lp=lpMatch(ViewGroup.LayoutParams.WRAP_CONTENT,0);lp.setMargins(0,dp(9),0,dp(4));content.addView(l,lp);
        EditText e=edit(label);e.setText(value);content.addView(e,lpMatch(dp(50),0));return e;
    }

    private EditText edit(String hint){
        EditText e=new EditText(this);e.setTextSize(13);e.setTextColor(INK);e.setHintTextColor(Color.rgb(150,160,174));e.setSingleLine(true);e.setHint(hint);e.setPadding(dp(12),0,dp(12),0);e.setBackground(round(Color.rgb(250,251,253),13));return e;
    }

    private Button primary(String text){
        Button b=new Button(this);b.setText(text);b.setTextSize(12);b.setTextColor(WHITE);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setBackground(round(GREEN,14));b.setMinHeight(dp(50));return b;
    }
    private Button primaryOutline(String text){
        Button b=new Button(this);b.setText(text);b.setTextSize(12);b.setTextColor(GREEN);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);GradientDrawable g=round(WHITE,14);g.setStroke(dp(1),GREEN);b.setBackground(g);b.setMinHeight(dp(50));return b;
    }
    private Button smallButton(String text,int color){
        Button b=new Button(this);b.setText(text);b.setTextSize(10);b.setTextColor(color);b.setAllCaps(false);GradientDrawable g=round(WHITE,13);g.setStroke(dp(1),light(color));b.setBackground(g);return b;
    }

    private void addThreeMini(View a,View b,View c){
        LinearLayout r=h();r.addView(a,weight());r.addView(b,weightMarginLeft());r.addView(c,weightMarginLeft());content.addView(r);
    }
    private void addFourQuick(View a,View b,View c,View d){
        LinearLayout r=h();r.addView(a,weight());r.addView(b,weightMarginLeft());r.addView(c,weightMarginLeft());r.addView(d,weightMarginLeft());content.addView(r);
    }

    private LinearLayout h(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout v(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView tv(String s,int sp,int color,int style){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(Typeface.DEFAULT,style);t.setLineSpacing(0,1.05f);return t;}
    private TextView pillText(String s,int sp,int color,int bg){TextView t=tv(s,sp,color,Typeface.BOLD);t.setBackground(round(bg,999));return t;}
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private int light(int color){
        int r=Color.red(color),g=Color.green(color),b=Color.blue(color);
        return Color.rgb((r+255*6)/7,(g+255*6)/7,(b+255*6)/7);
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private LinearLayout.LayoutParams lpMatch(int height,int marginTop){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);if(marginTop>0)p.setMargins(0,dp(marginTop),0,0);return p;}
    private LinearLayout.LayoutParams weight(){return new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);}
    private LinearLayout.LayoutParams weightMarginLeft(){LinearLayout.LayoutParams p=weight();p.setMargins(dp(7),0,0,0);return p;}
    private void spacer(int d){Space s=new Space(this);content.addView(s,new LinearLayout.LayoutParams(1,dp(d)));}
    private String money(long v){return NumberFormat.getNumberInstance(new Locale("ru","RU")).format(v)+" ₽";}
    private long parseLong(String s){try{return Long.parseLong(s.replaceAll("[^0-9]",""));}catch(Exception e){return 0;}}
    private String initials(String name){String[] p=name.trim().split("\\s+");return p.length>1?(""+p[0].charAt(0)+p[1].charAt(0)).toUpperCase():"ТК";}

    // ---------- models ----------
    static final class ObjectItem{
        final String id,address,client,workType,status,engineer,manager,installers;final int progress;final long contract,paid;
        ObjectItem(String id,String address,String client,String workType,String status,int progress,long contract,long paid,String engineer,String manager,String installers){
            this.id=id;this.address=address;this.client=client;this.workType=workType;this.status=status;this.progress=progress;this.contract=contract;this.paid=paid;this.engineer=engineer;this.manager=manager;this.installers=installers;
        }
    }
    static final class InstallerItem{
        final String id,name,uniformDate,status;final int workDays,daysOff,closedObjects,tools;final long accrued,paid;
        InstallerItem(String id,String name,int workDays,int daysOff,int closedObjects,long accrued,long paid,int tools,String uniformDate,String status){
            this.id=id;this.name=name;this.workDays=workDays;this.daysOff=daysOff;this.closedObjects=closedObjects;this.accrued=accrued;this.paid=paid;this.tools=tools;this.uniformDate=uniformDate;this.status=status;
        }
    }
    static final class MoneyTx{
        final String type,title,sub;final long amount;
        MoneyTx(String type,String title,String sub,long amount){this.type=type;this.title=title;this.sub=sub;this.amount=amount;}
    }
}
