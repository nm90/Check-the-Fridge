from django.conf.urls import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    url(r'^fridge/datetime', 'fridge.views.current_datetime', name='datetime'),
    url(r'^fridge/fridges', 'fridge.views.getFridge', name='fridges'),
    url(r'^fridge/login', 'fridge.views.fridge_login', name='login'),

    # url(r'^TheFridge/', include('TheFridge.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
     url(r'^fridge/admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
     url(r'^fridge/admin/', include(admin.site.urls)),
)
