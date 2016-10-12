from lingvodoc.views.v2.utils import (
    get_user_by_client_id
)

from pyramid.response import Response
from pyramid.view import view_config
from lingvodoc.models import DBSession, Dictionary, Locale
from pyramid.httpexceptions import (
    HTTPFound,
    HTTPOk
)
from pyramid.security import authenticated_userid
# from pyramid.chameleon_zpt import render_template_to_response
from pyramid.renderers import render_to_response

import sys
import multiprocessing

if sys.platform == 'darwin':
    multiprocessing.set_start_method('spawn')

import logging
log = logging.getLogger(__name__)


@view_config(route_name='testing', renderer='json')
def testing(request):
    # lang = DBSession.query(Language).filter_by(client_id=31, object_id=7).first()
    # return{'words': [{'lexical_entry': {'marked_for_deletion': False, 'published': False, 'came_from': None, 'parent_client_id': 5, 'parent_object_id': 1, 'object_id': 2, 'level': 'lexicalentry', 'client_id': 13, 'contains': [{'locale_id': 1, 'parent_client_id': 13, 'parent_object_id': 2, 'level': 'leveloneentity', 'additional_metadata': None, 'content': 'grouping word 0', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Word', 'object_id': 2, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 2, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:21 2016L5F93WF9UT', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 1, 'client_id': 13, 'contains': None}]}}, {'lexical_entry': {'marked_for_deletion': False, 'published': False, 'came_from': None, 'parent_client_id': 5, 'parent_object_id': 1, 'object_id': 3, 'level': 'lexicalentry', 'client_id': 13, 'contains': [{'locale_id': 1, 'parent_client_id': 13, 'parent_object_id': 3, 'level': 'leveloneentity', 'additional_metadata': None, 'content': 'grouping word 1', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Word', 'object_id': 3, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 3, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:22 2016FBTCGRTPV8', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 6, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 3, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:21 2016L5F93WF9UT', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 2, 'client_id': 13, 'contains': None}]}}, {'lexical_entry': {'marked_for_deletion': False, 'published': False, 'came_from': None, 'parent_client_id': 5, 'parent_object_id': 1, 'object_id': 6, 'level': 'lexicalentry', 'client_id': 13, 'contains': [{'locale_id': 1, 'parent_client_id': 13, 'parent_object_id': 6, 'level': 'leveloneentity', 'additional_metadata': None, 'content': 'grouping word 4', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Word', 'object_id': 6, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 6, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:21 2016L5F93WF9UT', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 7, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 6, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:22 2016FBTCGRTPV8', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 5, 'client_id': 13, 'contains': None}]}}, {'lexical_entry': {'marked_for_deletion': False, 'published': False, 'came_from': None, 'parent_client_id': 5, 'parent_object_id': 1, 'object_id': 4, 'level': 'lexicalentry', 'client_id': 13, 'contains': [{'locale_id': 1, 'parent_client_id': 13, 'parent_object_id': 4, 'level': 'leveloneentity', 'additional_metadata': None, 'content': 'grouping word 2', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Word', 'object_id': 4, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 4, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:22 2016FBTCGRTPV8', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 3, 'client_id': 13, 'contains': None}]}}, {'lexical_entry': {'marked_for_deletion': False, 'published': False, 'came_from': None, 'parent_client_id': 5, 'parent_object_id': 1, 'object_id': 5, 'level': 'lexicalentry', 'client_id': 13, 'contains': [{'locale_id': 1, 'parent_client_id': 13, 'parent_object_id': 5, 'level': 'leveloneentity', 'additional_metadata': None, 'content': 'grouping word 3', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Word', 'object_id': 5, 'client_id': 13, 'contains': None}, {'locale_id': None, 'parent_client_id': 13, 'parent_object_id': 5, 'level': 'groupingentity', 'additional_metadata': None, 'content': 'Mon Feb 15 16:42:22 2016FBTCGRTPV8', 'marked_for_deletion': False, 'published': False, 'entity_type': 'Etymology', 'object_id': 4, 'client_id': 13, 'contains': None}]}}]}
    return DBSession.query(Dictionary.__tablename__).all()

@view_config(route_name='main', renderer='templates/main.pt', request_method='GET')
def main_get(request):
    client_id = authenticated_userid(request)
    user = get_user_by_client_id(client_id)
    variables = {'client_id': client_id, 'user': user}
    return render_to_response('templates/main.pt', variables, request=request)


@view_config(route_name='all_statuses', renderer='json', request_method='GET')
def all_statuses(request):
    from pyramid.request import Request
    import json

    response = list()
    for status in ['WiP', 'Published', 'Limited access', 'Hidden']:

        subreq = Request.blank('/translation_service_search')
        subreq.method = 'POST'
        subreq.headers = request.headers
        subreq.json = {'searchstring': status}
        headers = {'Cookie': request.headers['Cookie']}
        subreq.headers = headers
        resp = request.invoke_subrequest(subreq)
        response.append(resp.json)
    request.response.status = HTTPOk.code
    return response


@view_config(route_name='all_locales', renderer='json', request_method='GET')
def all_locales(request):
    response = list()
    locales = DBSession.query(Locale).all()
    for locale in locales:
        locale_json = dict()
        locale_json['shortcut'] = locale.shortcut
        locale_json['intl_name'] = locale.intl_name
        locale_json['created_at'] = locale.created_at.timestamp()
        locale_json['id'] = locale.id
        response.append(locale_json)
    request.response.status = HTTPOk.code
    return response


@view_config(route_name='all_data_types', renderer='json', request_method='GET')
def all_data_types(request):
    from pyramid.request import Request
    import json

    response = list()
    for data_type in ['Text', 'Image', 'Sound', 'Praat markup', 'ELAN markup', 'Link']:

        subreq = Request.blank('/translation_service_search')
        subreq.method = 'POST'
        subreq.headers = request.headers
        subreq.json = {'searchstring': data_type}
        headers = {'Cookie': request.headers['Cookie']}
        subreq.headers = headers
        resp = request.invoke_subrequest(subreq)
        response.append(resp.json)
    request.response.status = HTTPOk.code
    return response


conn_err_msg = """\
Pyramid is having a problem using your SQL database.  The problem
might be caused by one of the following things:

1.  You may need to run the "initialize_lingvodoc_db" script
    to initialize your database tables.  Check your virtual
    environment's "bin" directory for this script and try to run it.

2.  Your database server may not be running.  Check that the
    database server referred to by the "sqlalchemy.url" setting in
    your "development.ini" file is running.

After you fix the problem, please restart the Pyramid application to
try it again.
"""