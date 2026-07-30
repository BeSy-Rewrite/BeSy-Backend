package de.hs_esslingen.besy.mail;

import java.util.Map;

import de.hs_esslingen.besy.enums.OrderStatus;

public final class OrderStatusPresentation {

        private static final String CTA_LABEL_VIEW_ORDER = "Bestellung ansehen";

        public final String badge;
        public final String bgColor;
        public final String textColor;
        public final String headline;
        public final String introText;
        public final String ctaLabel;

        private OrderStatusPresentation(String badge, String bgColor, String textColor,
                        String headline, String introText, String ctaLabel) {
                this.badge = badge;
                this.bgColor = bgColor;
                this.textColor = textColor;
                this.headline = headline;
                this.introText = introText;
                this.ctaLabel = ctaLabel;
        }

        private static final OrderStatusPresentation DEFAULT = new OrderStatusPresentation(
                        "Statusänderung", "#eef1f5", "#374151",
                        "Der Status einer Bestellung hat sich geändert",
                        "Der Status der folgenden Bestellung wurde aktualisiert.",
                        CTA_LABEL_VIEW_ORDER);

        private static final Map<OrderStatus, OrderStatusPresentation> MAP = Map.of(
                        OrderStatus.DEKAN_PENDING, new OrderStatusPresentation(
                                        "📋 Genehmigung angefordert", "#e0edff", "#193058",
                                        "Eine Bestellung wartet auf Ihre Genehmigung",
                                        "Bitte prüfen und genehmigen Sie die folgende Bestellung.",
                                        "Bestellung prüfen"),
                        OrderStatus.APPROVED, new OrderStatusPresentation(
                                        "✅ Genehmigt", "#e3f9e5", "#1b7f36",
                                        "Ihre Bestellung wurde genehmigt",
                                        "Die folgende Bestellung wurde genehmigt und kann nun weiterverarbeitet werden.",
                                        CTA_LABEL_VIEW_ORDER),
                        OrderStatus.IN_PROGRESS, new OrderStatusPresentation(
                                        "⏳ In Bearbeitung", "#fff4e0", "#a15c00",
                                        "Ihre Bestellung wird bearbeitet",
                                        "Der Status Ihrer Bestellung hat sich geändert.",
                                        CTA_LABEL_VIEW_ORDER),
                        OrderStatus.COMPLETED, new OrderStatusPresentation(
                                        "✔️ Fertiggestellt", "#eef1f5", "#374151",
                                        "Ihre Bestellung wurde fertiggestellt",
                                        "Der Status Ihrer Bestellung hat sich geändert.",
                                        CTA_LABEL_VIEW_ORDER));

        public static OrderStatusPresentation forStatus(OrderStatus status) {
                return MAP.getOrDefault(status, DEFAULT);
        }
}
