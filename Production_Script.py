from random import choices
import string
import requests

def generate_secret():
    return ''.join(choices(string.ascii_letters + string.digits, k=32))

def batch(endpoint, amount):
    devices = []
    print('Batching devices... Please wait.')
    for _ in range(amount):
        secret = generate_secret()
        resp = requests.post(endpoint, json={'name': 'Initialized by script', 'secret': secret})
        if resp.ok is False:
            print(f'Error registering device: {resp.text}')
            break
        else:
            devices.append((resp.json()['device'], secret))

    # Output as CSV for easy import into /etc/environment
    with open('devices.csv', 'w') as f:
        f.write('device_id,secret\n')
        for device_id, secret in devices:
            f.write(f'{device_id},{secret}\n')
    print(f'{len(devices)} devices registered successfully. Device information saved to devices.csv.')

def single(endpoint, secret=None):
    if secret is None:
        secret = input('Enter the device secret (leave blank to generate one): ')

    if not secret:
        secret = generate_secret()
    resp = requests.post(endpoint, json={'name': 'Initialized by script', 'secret': secret})
    if resp.ok is False:
        print(f'Error registering device: {resp.text}')
    else:
        print('Generated device id: ' + resp.json()['device'])
        print(f'Generated secret: {secret}')
        print(
            'Device registered successfully. Take this information and import it into your /etc/environment for use '
            'in the device configuration.')

def main():
    print('Welcome to the production script!')
    print('You can register new devices in batches or one at a time.')
    print('Please start by entering the endpoint address and port')
    endpoint = 'http://' + input('Endpoint address: ')
    endpoint += ':' + input('Port: ')
    endpoint += '/api/iot/register'
    print(f'Endpoint set to {endpoint}')
    while True:
        print('\nPlease select an option:')
        print('1. Batch registration')
        print('2. Single registration')
        print('3. Exit')
        choice = input('Enter your choice: ')
        if choice == '1':
            amount = int(input('Enter the number of devices to register: '))
            batch(endpoint, amount)
        elif choice == '2':
            single(endpoint)
        elif choice == '3':
            print('Exiting the production script. Goodbye!')
            break


if __name__ == "__main__":
    main()