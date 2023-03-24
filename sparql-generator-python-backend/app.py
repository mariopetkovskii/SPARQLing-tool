from flask import Flask, jsonify, request
from flask_cors import CORS
import requests
import re
from bs4 import BeautifulSoup

app = Flask(__name__)
CORS(app)


def camelcase_to_sentence(camelcase_string):
  words = re.findall(r'[A-Z]?[a-z]+|[A-Z]+(?=[A-Z]|$)', camelcase_string)
  sentence = ' '.join(words).capitalize()
  return sentence


def get_url_ontology(url):
  response = requests.get(url)
  html_content = response.content

  soup = BeautifulSoup(html_content, 'html.parser')

  uris = soup.select('.col-2')
  uri_list_1 = []
  uri_list_2 = []
  for uri in uris:
    print(uri.text)
    if not uri.text.startswith("is"):
      uri_list_1.append([uri.text.replace("\n", ""), camelcase_to_sentence(uri.text.replace("\n", "").split(':')[1])])
    else:
      uri_list_2.append([uri.text.replace("\n", "").replace("is ", "").replace(" of", ""), camelcase_to_sentence(uri.text.replace("\n", "").replace("is ", "").replace(" of", "").split(':')[1])])

    # uri_list_2.append()

  json_data_1 = []
  for uri in uri_list_1:
    obj = {
      "ontology": uri[0],
      "description": uri[1],
      "property": uri[0].split(":")[1]
    }
    json_data_1.append(obj)

  json_data_2 = []
  for uri in uri_list_2:
    obj = {
      "ontology": uri[0],
      "description": uri[1],
      "property": uri[0].split(":")[1]
    }
    json_data_2.append(obj)

  data = {"property": json_data_1, "isPropertyOf": json_data_2}

  return data

@app.route('/get-ontology')
def get_ontology():  # put application's code here
  url = 'https://dbpedia.org/page/' + request.args.get('resource')
  uri_list = get_url_ontology(url)

  return jsonify(uri_list)

@app.route('/get-page-ontology')
def get_ontology_with_url():
  url = request.args.get('resource')
  url.replace("/resource", "/page")
  uri_list = get_url_ontology(url)

  return jsonify(uri_list)

if __name__ == '__main__':
    app.run()
