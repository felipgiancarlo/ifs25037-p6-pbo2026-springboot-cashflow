package org.delcom.ifs25037springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Halaman publik "Tentang" yang menjelaskan aplikasi. Tidak memerlukan login.
 */
@Route("about")
@PageTitle("Tentang Cashflow")
public class AboutView extends Main {

    public AboutView() {
        getStyle().set("display", "block").set("padding", "2rem").set("max-width", "40rem").set("margin", "0 auto");
        add(new H1("Tentang Cashflow"));
        add(new Paragraph("Cashflow adalah aplikasi pencatat keuangan pribadi. Catat pemasukan dan "
                + "pengeluaran, pantau saldo, dan gunakan fitur Generate AI untuk menyusun transaksi "
                + "dari catatan teks bebas."));
        add(new RouterLink("Masuk ke aplikasi", LoginView.class));
    }
}