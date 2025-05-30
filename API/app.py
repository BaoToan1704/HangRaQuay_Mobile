from flask import Flask, request, jsonify, send_file
from scrapper import scrape_data, generate_word, generate_excel
import os

app = Flask(__name__)

@app.route('/get_data', methods=['GET'])
def get_data():
    mnv = request.args.get('mnv')
    template = request.args.get('template', 'phieu_xuat')  # default to phieu_xuat

    if not mnv:
        return jsonify({'error': 'Missing mnv'}), 400

    try:
        df = scrape_data(mnv, template)
        return jsonify(df.to_dict(orient='records'))
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/generate_export', methods=['POST', 'GET'])
def generate_export_api():
    if request.method == 'POST':
        content = request.json
        if not content:
            return {"error": "No data provided"}, 400

        data = content.get("data")
        template = content.get("template", "phieu_xuat")
        if not data:
            return {"error": "Missing 'data' in request"}, 400

        try:
            if template == "phieu_xuat":
                output_path = generate_word(data)
            elif template == "KPH":
                output_path = generate_excel(data)
            else:
                return {"error": "Unknown template type"}, 400

            return send_file(output_path, as_attachment=True)

        except Exception as e:
            return {"error": str(e)}, 500

    else:  # GET method
        mnv = request.args.get("mnv")
        template = request.args.get("template", "phieu_xuat")

        if not mnv:
            return {"error": "Missing mnv parameter"}, 400

        try:
            # Scrape data inside GET handler
            df = scrape_data(mnv, template)
            data = df.to_dict(orient='records')

            if template == "phieu_xuat":
                output_path = generate_word(data)
            elif template == "KPH":
                output_path = generate_excel(data)
            else:
                return {"error": "Unknown template type"}, 400

            return send_file(output_path, as_attachment=True)

        except Exception as e:
            return {"error": str(e)}, 500



if __name__ == '__main__':
    app.run(debug=False, host='0.0.0.0', port=10000)
