from django.conf.urls import url
from .views import Schedule

urlpatterns = [
    url(r'api/[vV](?P<api_level>[12])/school/(?P<school_id>[a-zA-Z_0-9]+)/?$',
        Schedule.as_view()),
]
