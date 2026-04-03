import json
import threading
import socket
import time
from http.server import HTTPServer, BaseHTTPRequestHandler


# Global state using threading primitives
_ready = threading.Event()
_alive = threading.Event()
_alive.set()  # alive by default
_last_message_time: float | None = None
_last_message_lock = threading.Lock()

MAX_IDLE_SECONDS = 300  # 5 minutes — tune to your use case


def set_ready(value: bool):
    if value:
        _ready.set()
    else:
        _ready.clear()


def set_alive(value: bool):
    if value:
        _alive.set()
    else:
        _alive.clear()


def record_message_processed():
    global _last_message_time
    with _last_message_lock:
        _last_message_time = time.time()


def _is_alive() -> bool:
    if not _alive.is_set():
        return False
    with _last_message_lock:
        if _last_message_time is not None:
            return (time.time() - _last_message_time) < MAX_IDLE_SECONDS
    return True


class HealthCheckHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        if self.path == '/health/live':
            self.handle_liveness()
        elif self.path == '/health/ready':
            self.handle_readiness()
        else:
            self.send_response(404)
            self.end_headers()

    def handle_liveness(self):
        ok = _is_alive()
        self.send_json(200 if ok else 503, {"status": "alive" if ok else "dead"})

    def handle_readiness(self):
        ok = _ready.is_set()
        self.send_json(200 if ok else 503, {"status": "ready" if ok else "not ready"})

    def send_json(self, status: int, body: dict):
        payload = json.dumps(body).encode()
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def log_message(self, format, *args):
        pass


def start_health_server(port: int = 8080) -> HTTPServer:
    server = HTTPServer(('0.0.0.0', port), HealthCheckHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    print(f"Health check server started on port {port}")
    return server


def wait_for_port(port: int, host: str = '0.0.0.0', timeout: float = 5.0) -> None:
    start = time.time()
    while time.time() - start < timeout:
        try:
            with socket.create_connection((host, port), timeout=1):
                return
        except OSError:
            time.sleep(0.1)
    raise RuntimeError(f"Port {port} not bound after {timeout}s")