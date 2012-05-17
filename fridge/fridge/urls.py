from django.conf.urls import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    url(r'^fridge/datetime', 'fridge.views.current_datetime', name='datetime'),
    url(r'^fridge/fridges', 'fridge.views.getFridge', name='fridges'),
    url(r'^fridge/login', 'fridge.views.fridge_login', name='login'),
    url(r'^fridge/search-fridge/$', 'fridge.views.search_form', name='search-fridge'),
    url(r'^fridge/search/$', 'fridge.views.search', name='search'),
    url(r'^fridge/search-id/$', 'fridge.views.search_id', name='search_id'),
    url(r'^fridge/search-upc/$', 'fridge.views.search_upc', name='search-upc'),
    url(r'^fridge/update-item', 'fridge.views.update_item', name='update-item'),

    # url(r'^TheFridge/', include('TheFridge.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
     url(r'^fridge/admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
     url(r'^fridge/admin/', include(admin.site.urls)),
)
