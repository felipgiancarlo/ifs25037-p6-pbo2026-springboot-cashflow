package org.delcom.ifs25037springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouterLink;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Halaman 404 yang aksesibel: punya title, landmark &lt;main&gt;, dan heading level-1,
 * sehingga audit Axe/Lighthouse tidak menandainya sebagai pelanggaran.
 */
public class NotFoundView extends Main implements HasErrorParameter<NotFoundException>, HasDynamicTitle {

    public NotFoundView() {
        getStyle().set("display", "block").set("padding", "2rem").set("max-width", "40rem").set("margin", "0 auto");
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        removeAll();
        add(new H1("Halaman tidak ditemukan"));
        add(new Paragraph("Halaman yang Anda cari tidak tersedia atau sudah dipindahkan."));
        add(new RouterLink("Kembali ke beranda", FinanceView.class));
        return HttpServletResponse.SC_NOT_FOUND;
    }

    @Override
    public String getPageTitle() {
        return "Halaman tidak ditemukan";
    }
}
