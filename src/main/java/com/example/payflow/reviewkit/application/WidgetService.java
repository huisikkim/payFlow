package com.example.payflow.reviewkit.application;

import com.example.payflow.reviewkit.domain.Business;
import com.example.payflow.reviewkit.domain.Widget;
import com.example.payflow.reviewkit.infrastructure.WidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("reviewKitWidgetService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetService {

    private final WidgetRepository widgetRepository;
    private final BusinessService businessService;

    @Transactional
    public Widget createWidget(Long businessId, String name, String theme, String layout, 
                               Integer displayLimit, String language) {
        Business business = businessService.getBusinessById(businessId);

        Widget widget = Widget.builder()
                .business(business)
                .name(name)
                .theme(theme != null ? theme : "light")
                .layout(layout != null ? layout : "grid")
                .displayLimit(displayLimit != null ? displayLimit : 6)
                .language(language != null ? language : "ko")
                .build();

        return widgetRepository.save(widget);
    }

    public Widget getWidgetByWidgetId(String widgetId) {
        return widgetRepository.findByWidgetId(widgetId)
                .orElseThrow(() -> new IllegalArgumentException("Widget not found: " + widgetId));
    }

    public List<Widget> getWidgetsByBusiness(Long businessId) {
        return widgetRepository.findByBusinessId(businessId);
    }

    @Transactional
    public void deleteWidget(Long widgetId) {
        widgetRepository.deleteById(widgetId);
    }
}
